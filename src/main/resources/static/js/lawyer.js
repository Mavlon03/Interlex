(function(){
  const loginForm = document.getElementById('loginForm');
  const loginError = document.getElementById('loginError');
  const loginCard = document.getElementById('loginCol');
  const dashboard = document.getElementById('dashboard');
  const assignedList = document.getElementById('assignedList');
  const availableList = document.getElementById('availableList');
  const assignedEmpty = document.getElementById('assignedEmpty');
  const availableEmpty = document.getElementById('availableEmpty');
  const logoutBtn = document.getElementById('logoutBtn');
  const lawyerEmail = document.getElementById('lawyerEmail');
  const refreshBtn = document.getElementById('refreshBtn');

  let currentLawyerId = null;
  let currentEmail = null;

  function setAuthToken(token) {
    if (token) {
      localStorage.setItem('authToken', token);
      axios.defaults.headers.common['Authorization'] = 'Bearer ' + token;
    } else {
      localStorage.removeItem('authToken');
      delete axios.defaults.headers.common['Authorization'];
    }
  }

  function showDashboard(email, lawyerId) {
    loginCard.classList.add('d-none');
    dashboard.classList.remove('d-none');
    lawyerEmail.textContent = email;
    currentLawyerId = lawyerId;
    currentEmail = email;
    localStorage.setItem('lawyerId', lawyerId);
    localStorage.setItem('lawyerEmail', email);
    loadMessages();
  }

  async function loadMessages() {
    assignedList.innerHTML = '<div class="text-muted">Yuklanmoqda...</div>';
    availableList.innerHTML = '<div class="text-muted">Yuklanmoqda...</div>';
    assignedEmpty.classList.add('d-none');
    availableEmpty.classList.add('d-none');

    try {
      if (!currentLawyerId) throw new Error('Lawyer ID missing. Iltimos qayta kiring.');

      // Fetch assigned messages (requires auth)
      const respAssigned = await axios.get('/api/contact/lawyer/' + currentLawyerId);
      const assigned = (respAssigned.data && respAssigned.data.success) ? respAssigned.data.data || [] : [];

      assignedList.innerHTML = '';
      if (assigned.length === 0) {
        assignedEmpty.classList.remove('d-none');
      } else {
        assigned.forEach(renderAssignedItem);
      }

      // Fetch lawyer details to determine specializations
      const respLawyer = await axios.get('/api/lawyers/' + currentLawyerId);
      const lawyer = (respLawyer.data && respLawyer.data.success) ? respLawyer.data.data : null;
      let specializations = [];
      if (lawyer && lawyer.specializations) {
        specializations = lawyer.specializations;
      }

      const available = [];
      for (const sp of specializations) {
        try {
          const r = await axios.get('/api/contact/service-type/' + sp);
          if (r.data && r.data.success) {
            const list = r.data.data || [];
            list.forEach(m => {
              if (!m.assignedLawyer) available.push(m);
            });
          }
        } catch (e) {
          // continue on error for individual service types
        }
      }

      availableList.innerHTML = '';
      if (available.length === 0) {
        availableEmpty.classList.remove('d-none');
      } else {
        // remove duplicates by id
        const uniq = {};
        for (const m of available) {
          if (!uniq[m.id]) { uniq[m.id] = true; renderAvailableItem(m); }
        }
      }

    } catch (err) {
      assignedList.innerHTML = '<div class="text-danger">Xatolik: ' + (err.message || 'Server error') + '</div>';
      availableList.innerHTML = '';
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

  function decodePayload(token) {
    try {
      const p = token.split('.')[1];
      const base64 = p.replace(/-/g, '+').replace(/_/g, '/');
      const json = decodeURIComponent(atob(base64).split('').map(function(c) {
        return '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2);
      }).join(''));
      return JSON.parse(json);
    } catch (e) { return null; }
  }

  function renderAssignedItem(m) {
    const el = document.createElement('div');
    el.className = 'list-group-item list-group-item-action';
    el.innerHTML = `
      <div class="d-flex w-100 justify-content-between align-items-start">
        <div>
          <h6 class="mb-1">${escapeHtml(m.subject)}</h6>
          <small class="text-muted">${escapeHtml(m.name)} • ${escapeHtml(m.email)} • ${escapeHtml(m.phone)}</small>
        </div>
        <div class="text-end">
          <span class="badge bg-info">${m.status}</span>
          <div><small class="text-muted">${new Date(m.createdAt).toLocaleString()}</small></div>
        </div>
      </div>
    `;
    el.addEventListener('click', () => openModal(m, false));
    assignedList.appendChild(el);
  }

  function renderAvailableItem(m) {
    const el = document.createElement('div');
    el.className = 'list-group-item list-group-item-action available-item';
    el.innerHTML = `
      <div class="d-flex w-100 justify-content-between align-items-start">
        <div>
          <h6 class="mb-1">${escapeHtml(m.subject)}</h6>
          <small class="text-muted">${escapeHtml(m.name)} • ${escapeHtml(m.email)}</small>
        </div>
        <div class="text-end">
          <span class="badge bg-warning text-dark">${m.serviceType}</span>
          <div><small class="text-muted">${new Date(m.createdAt).toLocaleString()}</small></div>
        </div>
      </div>
    `;
    el.addEventListener('click', () => openModal(m, true));
    availableList.appendChild(el);
  }

  function openModal(m, canClaim) {
    const modalEl = document.getElementById('msgModal');
    const title = document.getElementById('msgModalTitle');
    const body = document.getElementById('msgModalBody');
    const claimBtn = document.getElementById('modalClaimBtn');
    const respondBtn = document.getElementById('modalRespondBtn');
    const closeBtn = document.getElementById('modalCloseBtn');

    title.textContent = m.subject || 'Ariza';
    body.innerHTML = `
      <p><strong>Ism:</strong> ${escapeHtml(m.name)}</p>
      <p><strong>Email:</strong> ${escapeHtml(m.email)}</p>
      <p><strong>Telefon:</strong> ${escapeHtml(m.phone)}</p>
      <p><strong>Xabar:</strong><br>${escapeHtml(m.message)}</p>
      <p class="text-muted"><small>ID: ${m.id} • Holat: ${m.status} • Turi: ${m.serviceType}</small></p>
    `;

    if (canClaim) { claimBtn.classList.remove('d-none'); claimBtn.onclick = () => claimMessage(m.id); }
    else { claimBtn.classList.add('d-none'); claimBtn.onclick = null; }

    respondBtn.onclick = async () => { await updateStatus(m.id, 'RESPONDED'); const modal = bootstrap.Modal.getInstance(modalEl); modal.hide(); };
    closeBtn.onclick = async () => { await updateStatus(m.id, 'CLOSED'); const modal = bootstrap.Modal.getInstance(modalEl); modal.hide(); };
  }

  async function updateStatus(id, status) {
    try {
      const resp = await axios.put('/api/contact/' + id + '/status?status=' + status);
      if (resp.data && resp.data.success) {
        await loadMessages();
      }
    } catch (e) {
      alert('Xatolik: ' + (e.response?.data?.message || e.message));
    }
  }

  async function claimMessage(id) {
    try {
      const resp = await axios.put('/api/contact/' + id + '/assign/' + currentLawyerId);
      if (resp.data && resp.data.success) {
        await loadMessages();
      }
    } catch (e) {
      alert('Xatolik: ' + (e.response?.data?.message || e.message));
    }
  }

  // Login handler (replaces earlier simple handler)
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
        // server returns lawyerId and email in payload
        showDashboard(resp.data.data.email, resp.data.data.lawyerId);
      } else {
        throw new Error(resp.data?.message || 'Login failed');
      }
    } catch (err) {
      loginError.textContent = err.response?.data?.message || err.message || 'Login failed';
      loginError.classList.remove('d-none');
    }
  });

  // restore token/session if exists
  const existingToken = localStorage.getItem('authToken');
  const savedLawyerId = localStorage.getItem('lawyerId');
  const savedEmail = localStorage.getItem('lawyerEmail');
  if (existingToken && savedLawyerId && savedEmail) {
    setAuthToken(existingToken);
    const payload = decodePayload(existingToken);
    if (payload && payload.sub) {
      showDashboard(savedEmail, savedLawyerId);
    } else {
      setAuthToken(null);
      localStorage.removeItem('lawyerId');
      localStorage.removeItem('lawyerEmail');
    }
  }

  // extra handlers
  refreshBtn.addEventListener('click', loadMessages);
  logoutBtn.addEventListener('click', function(){
    setAuthToken(null);
    dashboard.classList.add('d-none');
    loginCard.classList.remove('d-none');
    localStorage.removeItem('lawyerId');
    localStorage.removeItem('lawyerEmail');
    currentLawyerId = null;
  });

  // render helpers & modal actions are declared earlier via function references
})();