(function(){
  const loginForm = document.getElementById('loginForm');
  const loginError = document.getElementById('loginError');
  const loginCard = document.getElementById('loginCard');
  const dashboard = document.getElementById('dashboard');
  const messagesList = document.getElementById('messagesList');
  const logoutBtn = document.getElementById('logoutBtn');
  const lawyerEmail = document.getElementById('lawyerEmail');

  function setAuthToken(token) {
    if (token) {
      localStorage.setItem('authToken', token);
      axios.defaults.headers.common['Authorization'] = 'Bearer ' + token;
    } else {
      localStorage.removeItem('authToken');
      delete axios.defaults.headers.common['Authorization'];
    }
  }

  function showDashboard(email) {
    loginCard.classList.add('d-none');
    dashboard.classList.remove('d-none');
    lawyerEmail.textContent = email;
    loadMessages();
  }

  async function loadMessages() {
    messagesList.innerHTML = '<div class="text-muted">Yuklanmoqda...</div>';
    try {
      const token = localStorage.getItem('authToken');
      if (!token) throw new Error('Not authenticated');

      // get lawyerId from token payload (naive parse)
      const payload = JSON.parse(atob(token.split('.')[1]));
      const email = payload.sub;

      // fetch lawyer by email to get id
      const respLawyers = await axios.get('/api/lawyers?email='+encodeURIComponent(email));
      if(!respLawyers.data || !respLawyers.data.success) throw new Error('Could not get lawyer info');
      const lawyer = respLawyers.data.data[0];
      const lawyerId = lawyer.id;

      const resp = await axios.get('/api/contact/lawyer/' + lawyerId);
      if (resp.data && resp.data.success) {
        const messages = resp.data.data || [];
        if (messages.length === 0) {
          messagesList.innerHTML = '<div class="text-muted">Arizalar topilmadi</div>';
          return;
        }

        messagesList.innerHTML = '';
        messages.forEach(m => {
          const item = document.createElement('div');
          item.className = 'list-group-item';
          item.innerHTML = `
            <div class="d-flex w-100 justify-content-between">
              <h5 class="mb-1">${escapeHtml(m.subject)}</h5>
              <small>${new Date(m.createdAt).toLocaleString()}</small>
            </div>
            <p class="mb-1">${escapeHtml(m.message)}</p>
            <small>${escapeHtml(m.name)} • ${escapeHtml(m.phone)} • ${escapeHtml(m.email)} • <strong>${m.status}</strong></small>
            <div class="mt-2">
              <button class="btn btn-sm btn-success me-2" data-action="respond" data-id="${m.id}">Javob berdi</button>
              <button class="btn btn-sm btn-danger" data-action="close" data-id="${m.id}">Yopildi</button>
            </div>
          `;
          messagesList.appendChild(item);
        });
      }
    } catch (err) {
      messagesList.innerHTML = '<div class="text-danger">Xatolik: ' + (err.message || 'Server error') + '</div>';
    }
  }

  loginForm.addEventListener('submit', async function(e){
    e.preventDefault();
    loginError.classList.add('d-none');

    const email = document.getElementById('loginEmail').value.trim();
    const password = document.getElementById('loginPassword').value;

    try {
      const resp = await axios.post('/api/auth/login', { email, password });
      if (resp.data && resp.data.success) {
        const token = resp.data.data.token;
        setAuthToken(token);
        showDashboard(resp.data.data.email);
      } else {
        throw new Error(resp.data?.message || 'Login failed');
      }
    } catch (err) {
      loginError.textContent = err.response?.data?.message || err.message || 'Login failed';
      loginError.classList.remove('d-none');
    }
  });

  logoutBtn.addEventListener('click', function(){
    setAuthToken(null);
    dashboard.classList.add('d-none');
    loginCard.classList.remove('d-none');
  });

  messagesList.addEventListener('click', async function(e){
    const btn = e.target.closest('button');
    if (!btn) return;
    const action = btn.getAttribute('data-action');
    const id = btn.getAttribute('data-id');
    if (!action || !id) return;

    try {
      let status;
      if (action === 'respond') status = 'RESPONDED';
      if (action === 'close') status = 'CLOSED';
      const resp = await axios.put('/api/contact/' + id + '/status?status=' + status);
      if (resp.data && resp.data.success) {
        await loadMessages();
      }
    } catch (err) {
      alert('Xatolik: ' + (err.response?.data?.message || err.message));
    }
  });

  function escapeHtml(unsafe) {
    if (!unsafe) return '';
    return unsafe
      .replace(/&/g, "&amp;")
      .replace(/</g, "&lt;")
      .replace(/>/g, "&gt;")
      .replace(/\"/g, "&quot;")
      .replace(/'/g, "&#039;");
  }

  // restore token if exists
  const existingToken = localStorage.getItem('authToken');
  if (existingToken) {
    setAuthToken(existingToken);
    // show dashboard after validating token
    try {
      const payload = JSON.parse(atob(existingToken.split('.')[1]));
      showDashboard(payload.sub || '');
    } catch (e) {
      setAuthToken(null);
    }
  }
})();