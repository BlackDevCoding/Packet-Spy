(() => {
  const tbody = document.querySelector('#tbl tbody');
  const filter = document.querySelector('#filter');
  const pause = document.querySelector('#pause');
  const clearBtn = document.querySelector('#clear');
  const count = document.querySelector('#count');
  let total = 0, visible = 0;
  function updateCount(){ count.textContent = `${visible} shown`; }
  function add(ev){
    const tr = document.createElement('tr');
    tr.innerHTML = `<td>${ev.ts}</td><td class="${ev.direction==='IN'?'dir-in':'dir-out'}">${ev.direction}</td><td>${ev.class}</td>`;
    if (!matches(ev)) tr.style.display = 'none';
    tbody.appendChild(tr);
    total++;
    visible = [...tbody.children].filter(r => r.style.display !== 'none').length;
    if (total > 10000) while (tbody.children.length > 10000) tbody.removeChild(tbody.firstChild);
    updateCount();
    tr.scrollIntoView({block:'end'});
  }
  function matches(ev){
    const q = filter.value.trim().toLowerCase();
    return !q || (ev.class||'').toLowerCase().includes(q);
  }
  filter.addEventListener('input', () => {
    const q = filter.value.trim().toLowerCase();
    visible = 0;
    [...tbody.children].forEach(tr => {
      const klass = tr.children[2].textContent.toLowerCase();
      const keep = !q || klass.includes(q);
      tr.style.display = keep ? '' : 'none';
      if (keep) visible++;
    });
    updateCount();
  });
  clearBtn.addEventListener('click', () => { tbody.innerHTML=''; total=visible=0; updateCount(); });
  function connect(){
    const es = new EventSource('/events');
    es.onmessage = (e) => {
      if (pause.checked) return;
      try { add(JSON.parse(e.data)); } catch {}
    };
    es.onerror = () => { es.close(); setTimeout(connect, 1500); };
  }
  updateCount();
  connect();
})();