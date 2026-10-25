#!/usr/bin/env bash
# One-time preparation of the shared server for Interlex on maxlegal.uz.
# It only adds /opt/interlex and its own nginx file (interlex.conf), replaces the temporary
# "tez orada" placeholder (maxlegal-root.conf) and never touches other projects.
#
#   sudo bash setup-server.sh <domain> <app_port> <certbot_email>
#   sudo bash setup-server.sh maxlegal.uz 8095 you@example.com
set -euo pipefail

DOMAIN="${1:?domain}"
APP_PORT="${2:?app port}"
EMAIL="${3:?email for Lets Encrypt certificates}"
BASE_DIR="/opt/interlex"
NAME="interlex"
SITE_FILE="/etc/nginx/sites-available/${NAME}.conf"
SITE_LINK="/etc/nginx/sites-enabled/${NAME}.conf"
PLACEHOLDER_LINK="/etc/nginx/sites-enabled/maxlegal-root.conf"
SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
TEMPLATE="${SCRIPT_DIR}/../nginx/maxlegal.conf.template"
[[ -f "$TEMPLATE" ]] || TEMPLATE="${SCRIPT_DIR}/maxlegal.conf.template"

say()  { printf '\n\033[1;32m==> %s\033[0m\n' "$*"; }
fail() { printf '\n\033[1;31mXATO: %s\033[0m\n' "$*"; exit 1; }

[[ $EUID -eq 0 ]] || fail "root sifatida ishga tushiring (sudo)"
for cmd in docker nginx certbot envsubst ss; do
    command -v "$cmd" >/dev/null || fail "'$cmd' topilmadi"
done
docker compose version >/dev/null || fail "docker compose plugin topilmadi"
[[ -f "$TEMPLATE" ]] || fail "nginx shabloni topilmadi: $TEMPLATE"

say "Port ${APP_PORT} bo'shligini tekshirish"
if ss -tlnH "sport = :${APP_PORT}" | grep -q .; then
    docker ps --format '{{.Names}} {{.Ports}}' | grep -q "^interlex-app.*:${APP_PORT}->" \
        || fail "Port ${APP_PORT} band. Boshqa port tanlang: ss -tulpn | grep LISTEN"
fi

say "Papka: ${BASE_DIR}"
mkdir -p "${BASE_DIR}"
chmod 700 "${BASE_DIR}"

say "SSL sertifikat: ${DOMAIN} + www.${DOMAIN}"
if [[ ! -f "/etc/letsencrypt/live/${DOMAIN}/fullchain.pem" ]]; then
    certbot certonly --nginx -d "$DOMAIN" -d "www.$DOMAIN" --non-interactive --agree-tos -m "$EMAIL"
else
    echo "Sertifikat bor: /etc/letsencrypt/live/${DOMAIN}"
fi
[[ -f /etc/letsencrypt/options-ssl-nginx.conf && -f /etc/letsencrypt/ssl-dhparams.pem ]] \
    || fail "certbot nginx fayllari topilmadi (/etc/letsencrypt/options-ssl-nginx.conf)"

say "Nginx konfiguratsiyasi: ${SITE_FILE}"
if [[ -e "$SITE_FILE" ]] && ! grep -q "Interlex (maxlegal.uz)" "$SITE_FILE"; then
    fail "${SITE_FILE} boshqa loyihaga tegishli, tegmayman"
fi
# the domain may only live in our own file or in the placeholder we are replacing
if grep -RlE "server_name[^;]*(^|[[:space:]])(www\.)?${DOMAIN//./\\.}([[:space:]]|;)" /etc/nginx/sites-enabled/ 2>/dev/null \
        | grep -vE "/(${NAME}|maxlegal-root)\.conf$" | grep -q .; then
    fail "${DOMAIN} boshqa nginx konfiguratsiyasida bor"
fi

BACKUP=""
[[ -f "$SITE_FILE" ]] && BACKUP="$(mktemp)" && cp "$SITE_FILE" "$BACKUP"
PLACEHOLDER_WAS_ON=false
[[ -L "$PLACEHOLDER_LINK" ]] && PLACEHOLDER_WAS_ON=true

export DOMAIN APP_PORT
envsubst '${DOMAIN} ${APP_PORT}' < "$TEMPLATE" > "$SITE_FILE"
ln -sf "$SITE_FILE" "$SITE_LINK"
rm -f "$PLACEHOLDER_LINK"

if ! nginx -t; then
    if [[ -n "$BACKUP" ]]; then cp "$BACKUP" "$SITE_FILE"; else rm -f "$SITE_FILE" "$SITE_LINK"; fi
    $PLACEHOLDER_WAS_ON && ln -sf /etc/nginx/sites-available/maxlegal-root.conf "$PLACEHOLDER_LINK"
    fail "nginx -t xato berdi, o'zgarish bekor qilindi"
fi
systemctl reload nginx

say "Tayyor"
cat <<EOF
  Domen:  https://${DOMAIN}  (www -> asosiy domen)
  Ilova:  127.0.0.1:${APP_PORT}  (konteyner hali yo'q bo'lsa 502 — CI deploy qilgach ochiladi)
  Papka:  ${BASE_DIR}   (CI shu yerga docker-compose.yml va .env yozadi)
EOF
