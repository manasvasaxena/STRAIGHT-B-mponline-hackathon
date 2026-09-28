/* LearnQuest MP Web - Interactive Logic with Parity to Kotlin App */

// Global App State
let state = {
  isAirplaneMode: true,
  activeProfileId: 'p1',
  profiles: [
    { id: 'p1', name: 'Aarav Sharma', grade: 'Class 8', lang: 'हिन्दी', xp: 450, streak: 5, isProtected: true },
    { id: 'p2', name: 'Priya Tribal', grade: 'Class 7', lang: 'English', xp: 280, streak: 3, isProtected: true }
  ],
  levels: [
    { id: 'l1', titleHi: 'विज्ञान: प्रकाश एवं परावर्तन', titleEn: 'Science: Light & Reflection', subject: 'Science', level: 1, total: 5, downloaded: 5, size: 2.4, isDownloaded: true, mastery: 'Mastered' },
    { id: 'l2', titleHi: 'गणित: बीजगणित मूल बातें', titleEn: 'Math: Algebra Fundamentals', subject: 'Mathematics', level: 2, total: 6, downloaded: 0, size: 3.1, isDownloaded: false, mastery: 'Weak' },
    { id: 'l3', titleHi: 'पर्यावरण अध्ययन', titleEn: 'Environmental Studies', subject: 'EVS', level: 3, total: 4, downloaded: 4, size: 1.8, isDownloaded: true, mastery: 'Developing' }
  ],
  questions: [
    { id: 'q1', text: 'प्रकाश की गति कितनी होती है? (What is the speed of light?)', options: ['3 x 10^8 m/s', '3 x 10^6 m/s', '3000 m/s', '300 km/s'], correct: 0 },
    { id: 'q2', text: 'पौधों में प्रकाश संश्लेषण के लिए क्या आवश्यक है?', options: ['सूर्य का प्रकाश', 'जल', 'कार्बन डाइऑक्साइड', 'उपरोक्त सभी'], correct: 3 }
  ],
  doubts: [
    { id: 'd1', question: 'प्रकाश का परावर्तन क्या है?', resolvedOffline: true, answer: 'जब प्रकाश किसी चिकनी सतह से टकराकर वापस लौटता है तो इसे परावर्तन कहते हैं। (Cached FAQ)', escalated: false },
    { id: 'd2', question: 'बीजगणित में "x" का मान कैसे निकालते हैं?', resolvedOffline: false, answer: 'ऑनलाइन RAG या शिक्षक से उत्तर की प्रतीक्षा है।', escalated: true }
  ],
  syncQueue: [
    { id: 's1', action: 'LESSON_COMPLETE', payload: "{levelId: 'l1', lesson: 5}", status: 'PENDING' },
    { id: 's2', action: 'XP_ADD', payload: '{xp: 50, streakProtected: true}', status: 'PENDING' }
  ],
  scholarships: [
    { title: 'MP Tribal Welfare Post-Matric Scholarship', provider: 'Government of MP', category: 'Tribal / ST', eligibility: 'Class 9-12 ST students in MP', amount: '₹8,000 / year', deadline: '31 Oct 2026', verified: true },
    { title: 'Rural Science Talent Search', provider: 'MP Education Dept', category: 'Merit Cum Means', eligibility: 'Score ≥ 75% in Class 8', amount: '₹12,000 / year', deadline: '15 Nov 2026', verified: true }
  ],
  careers: [
    { title: 'Agriculture Extension Officer', sector: 'Government / Agri-tech', edu: 'B.Sc Agriculture', desc: 'Help farmers adopt modern sustainable agricultural practices.', subjects: ['Science', 'Biology', 'Environment'] },
    { title: 'Solar Energy Technician', sector: 'Renewable Energy', edu: 'ITI / Diploma', desc: 'Install and maintain solar panels in rural off-grid setups.', subjects: ['Physics', 'Mathematics'] }
  ],
  quizCurrentIdx: 0,
  quizAnswers: {},
  quizSubmitted: false
};

// Navigation
function navTo(screenId, btnElement) {
  document.querySelectorAll('.screen').forEach(s => s.classList.remove('active'));
  document.querySelectorAll('.nav-item').forEach(b => b.classList.remove('active'));
  document.getElementById(screenId).classList.add('active');
  if (btnElement) btnElement.classList.add('active');
}

// Toggle Offline Mode
function toggleAirplaneMode(isOffline) {
  state.isAirplaneMode = isOffline;
  const header = document.getElementById('topHeader');
  const modeText = document.getElementById('modeText');
  
  if (isOffline) {
    header.classList.add('offline');
    modeText.innerText = 'OFFLINE MODE (ऑफलाइन)';
    modeText.style.color = '#FFC107';
  } else {
    header.classList.remove('offline');
    modeText.innerText = 'ONLINE MODE (ऑनलाइन)';
    modeText.style.color = '#E2E8F0';
  }
  renderAll();
}

// Render Functions
function renderAll() {
  renderHeader();
  renderHome();
  renderQuiz();
  renderDoubts();
  renderOpportunities('scholarships');
  renderProfiles();
  renderSyncQueue();
}

function getActiveProfile() {
  return state.profiles.find(p => p.id === state.activeProfileId) || state.profiles[0];
}

function renderHeader() {
  const p = getActiveProfile();
  document.getElementById('profileHeaderName').innerText = `छात्र / Student: ${p.name}`;
  document.getElementById('xpBadge').innerText = `⚡ ${p.xp} XP`;
  document.getElementById('streakBadge').innerText = p.isProtected ? `🛡️ Streak Shield (${p.streak} Days)` : `🔥 Streak: ${p.streak} Days`;
  
  const pendingCount = state.syncQueue.filter(q => q.status === 'PENDING').length;
  const syncBadge = document.getElementById('syncBadge');
  syncBadge.innerText = `🔄 Sync: ${pendingCount} Queue`;
  if (pendingCount === 0) {
    syncBadge.classList.add('synced');
  } else {
    syncBadge.classList.remove('synced');
  }
}

function renderHome() {
  const container = document.getElementById('levelsList');
  const banner = document.getElementById('homeOfflineBanner');
  banner.style.display = state.isAirplaneMode ? 'block' : 'none';

  container.innerHTML = state.levels.map(lvl => `
    <div class="card">
      <div style="display:flex; justify-content:space-between; align-items:center;">
        <div>
          <strong>Level ${lvl.level}: ${lvl.titleHi}</strong><br/>
          <small style="color:gray;">${lvl.titleEn} (${lvl.subject})</small>
        </div>
        <span style="font-size:11px; font-weight:bold; padding:3px 6px; border-radius:4px; 
          background:${lvl.mastery === 'Mastered' ? '#DCFCE7' : lvl.mastery === 'Developing' ? '#FEF3C7' : '#FEE2E2'};
          color:${lvl.mastery === 'Mastered' ? '#166534' : lvl.mastery === 'Developing' ? '#92400E' : '#991B1B'};">
          ${lvl.mastery}
        </span>
      </div>
      <div style="display:flex; justify-content:space-between; align-items:center; margin-top:12px;">
        <small>Lessons: ${lvl.downloaded}/${lvl.total} | ${lvl.size} MB</small>
        ${lvl.isDownloaded 
          ? `<button class="btn btn-secondary" onclick="alert('Starting downloaded lesson...')">अध्ययन करें (Start)</button>`
          : `<button class="btn" ${state.isAirplaneMode ? 'disabled' : ''} onclick="downloadLevel('${lvl.id}')">डाउनलोड (${lvl.size} MB)</button>`}
      </div>
    </div>
  `).join('');
}

function downloadLevel(id) {
  state.levels = state.levels.map(l => l.id === id ? { ...l, isDownloaded: true, downloaded: l.total } : l);
  renderAll();
}

function renderQuiz() {
  const container = document.getElementById('quizContent');
  if (state.quizSubmitted) {
    let score = 0;
    state.questions.forEach((q, idx) => {
      if (state.quizAnswers[idx] === q.correct) score++;
    });
    const pct = Math.round((score / state.questions.length) * 100);
    const mastery = pct >= 85 ? 'Mastered (≥ 85%)' : pct >= 60 ? 'Developing (60-84%)' : 'Weak (< 60%)';

    container.innerHTML = `
      <div class="card" style="text-align:center;">
        <h3>परिणाम / Quiz Score</h3>
        <h1 style="font-size:42px; color:var(--saffron-primary); margin:10px 0;">${pct}%</h1>
        <p><strong>Mastery State:</strong> ${mastery}</p>
        <button class="btn btn-secondary" style="margin-top:16px;" onclick="resetQuiz()">पुनः प्रयास करें / Repeat Practice</button>
      </div>
    `;
    return;
  }

  const q = state.questions[state.quizCurrentIdx];
  const selected = state.quizAnswers[state.quizCurrentIdx];

  container.innerHTML = `
    <small style="color:gray;">प्रश्न ${state.quizCurrentIdx + 1} / ${state.questions.length}</small>
    <div class="card" style="margin-top:8px;">
      <p><strong>${q.text}</strong></p>
    </div>
    ${q.options.map((opt, idx) => `
      <label class="option-item">
        <input type="radio" name="opt" value="${idx}" ${selected === idx ? 'checked' : ''} onchange="selectQuizOption(${idx})">
        <span>${opt}</span>
      </label>
    `).join('')}
    <button class="btn" style="width:100%; margin-top:16px;" ${selected === undefined ? 'disabled' : ''} onclick="nextQuizStep()">
      ${state.quizCurrentIdx < state.questions.length - 1 ? 'अगला प्रश्न (Next)' : 'सबमिट करें (Submit)'}
    </button>
  `;
}

function selectQuizOption(idx) {
  state.quizAnswers[state.quizCurrentIdx] = idx;
  renderQuiz();
}

function nextQuizStep() {
  if (state.quizCurrentIdx < state.questions.length - 1) {
    state.quizCurrentIdx++;
    renderQuiz();
  } else {
    state.quizSubmitted = true;
    // Award XP
    const active = getActiveProfile();
    active.xp += 50;
    state.syncQueue.push({ id: 's' + Date.now(), action: 'QUIZ_RESULT', payload: '{score: 100}', status: 'PENDING' });
    renderAll();
  }
}

function resetQuiz() {
  state.quizCurrentIdx = 0;
  state.quizAnswers = {};
  state.quizSubmitted = false;
  renderQuiz();
}

function renderDoubts() {
  document.getElementById('doubtModeBanner').innerText = state.isAirplaneMode 
    ? '⚡ OFFLINE MODE: Answers are served from cached curriculum FAQs & offline knowledge base.'
    : '🌐 ONLINE MODE: Connected to Curriculum RAG Engine with LLM synthesis.';
  document.getElementById('doubtModeBanner').className = `banner ${state.isAirplaneMode ? 'warning' : 'info'}`;

  const container = document.getElementById('doubtsList');
  container.innerHTML = state.doubts.map(d => `
    <div class="card">
      <strong>Q: ${d.question}</strong>
      <div style="background:#F3F4F6; padding:8px; border-radius:6px; margin:8px 0; font-size:13px; color:#374151;">
        ${d.answer}
      </div>
      <div style="display:flex; justify-content:space-between; align-items:center;">
        <small style="font-weight:bold; color:${d.resolvedOffline ? '#166534' : '#D97706'}">
          ${d.resolvedOffline ? '✓ Offline Match (कैश्ड उत्तर)' : '⚠️ Requires Online RAG'}
        </small>
        ${d.escalated 
          ? `<small style="color:gray; font-weight:bold;">📩 Teacher Escalated</small>`
          : `<button class="btn" style="padding:4px 8px; font-size:11px;" onclick="escalateDoubt('${d.id}')">शिक्षक को भेजें (Escalate)</button>`}
      </div>
    </div>
  `).join('');
}

function submitDoubt() {
  const input = document.getElementById('doubtInput');
  if (!input.value.trim()) return;

  const newD = {
    id: 'd' + Date.now(),
    question: input.value,
    resolvedOffline: state.isAirplaneMode,
    answer: state.isAirplaneMode ? 'कैश्ड उत्तर: प्रकाश सीधी रेखा में गमन करता है।' : 'RAG AI Result: Detailed online synthesized response.',
    escalated: false
  };
  state.doubts.unshift(newD);
  input.value = '';
  renderAll();
}

function escalateDoubt(id) {
  state.doubts = state.doubts.map(d => d.id === id ? { ...d, escalated: true } : d);
  state.syncQueue.push({ id: 's' + Date.now(), action: 'DOUBT_ESCALATE', payload: `{doubtId: '${id}'}`, status: 'PENDING' });
  renderAll();
}

function switchOppTab(tab) {
  document.getElementById('btnTabScholarships').style.background = tab === 'scholarships' ? 'var(--saffron-primary)' : '#6B7280';
  document.getElementById('btnTabCareers').style.background = tab === 'careers' ? 'var(--saffron-primary)' : '#6B7280';
  renderOpportunities(tab);
}

function renderOpportunities(tab) {
  const container = document.getElementById('oppList');
  if (tab === 'scholarships') {
    container.innerHTML = state.scholarships.map(s => `
      <div class="card">
        <div style="display:flex; justify-content:space-between;">
          <strong>${s.title}</strong>
          <span style="font-size:10px; background:#DCFCE7; color:#166534; padding:2px 6px; border-radius:4px; font-weight:bold;">✓ Verified DB</span>
        </div>
        <small style="color:gray;">Provider: ${s.provider} | Category: ${s.category}</small>
        <p style="font-size:13px; margin:6px 0;">Eligibility: ${s.eligibility}</p>
        <div style="display:flex; justify-content:space-between; font-weight:bold; font-size:12px;">
          <span style="color:var(--forest-green);">Amount: ${s.amount}</span>
          <span style="color:red;">Deadline: ${s.deadline}</span>
        </div>
      </div>
    `).join('');
  } else {
    container.innerHTML = state.careers.map(c => `
      <div class="card">
        <strong>${c.title}</strong><br/>
        <small style="color:gray;">Sector: ${c.sector} | Required: ${c.edu}</small>
        <p style="font-size:13px; margin:6px 0;">${c.desc}</p>
        <small style="color:var(--saffron-primary); font-weight:bold;">Recommended: ${c.subjects.join(', ')}</small>
      </div>
    `).join('');
  }
}

function renderProfiles() {
  const container = document.getElementById('profilesList');
  container.innerHTML = state.profiles.map(p => `
    <div class="card" style="background:${p.id === state.activeProfileId ? '#FEF3C7' : '#FFF'}; border: ${p.id === state.activeProfileId ? '2px solid var(--saffron-primary)' : '1px solid #F3F4F6'};">
      <div style="display:flex; justify-content:space-between; align-items:center;">
        <div>
          <strong>${p.name} ${p.id === state.activeProfileId ? '(सक्रिय / Active)' : ''}</strong><br/>
          <small style="color:gray;">${p.grade} | ${p.lang}</small><br/>
          <small>⚡ XP: ${p.xp} | 🔥 Streak: ${p.streak} Days</small>
        </div>
        <input type="radio" name="activeP" ${p.id === state.activeProfileId ? 'checked' : ''} onchange="switchProfile('${p.id}')">
      </div>
    </div>
  `).join('');
}

function switchProfile(id) {
  state.activeProfileId = id;
  renderAll();
}

function showAddProfileModal() {
  const name = prompt('नाम दर्ज करें (Enter Student Name):');
  if (name) {
    const newP = { id: 'p' + Date.now(), name: name, grade: 'Class 8', lang: 'हिन्दी', xp: 0, streak: 0, isProtected: true };
    state.profiles.push(newP);
    state.activeProfileId = newP.id;
    renderAll();
  }
}

function renderSyncQueue() {
  const container = document.getElementById('syncQueueList');
  const banner = document.getElementById('syncBanner');
  const btnSync = document.getElementById('btnSyncNow');

  banner.className = `banner ${state.isAirplaneMode ? 'warning' : 'success'}`;
  banner.innerText = state.isAirplaneMode 
    ? '⚠️ Connectivity is OFF. All local actions (XP, Quiz score, Lessons) are safe in local sync queue.'
    : '✅ Network Available. Ready to push delta updates to cloud backend with additive merge rules.';

  btnSync.disabled = state.isAirplaneMode || state.syncQueue.length === 0;

  if (state.syncQueue.length === 0) {
    container.innerHTML = '<p style="text-align:center; color:gray; padding:20px;">No pending delta updates! All data synchronized.</p>';
    return;
  }

  container.innerHTML = state.syncQueue.map(q => `
    <div class="card" style="display:flex; justify-content:space-between; align-items:center;">
      <div>
        <strong>Action: ${q.action}</strong><br/>
        <small style="color:gray;">Payload: ${q.payload}</small>
      </div>
      <span style="font-size:11px; font-weight:bold; padding:3px 8px; border-radius:4px; 
        background:${q.status === 'PENDING' ? '#FEF3C7' : '#DCFCE7'};
        color:${q.status === 'PENDING' ? '#D97706' : '#166534'};">
        ${q.status}
      </span>
    </div>
  `).join('');
}

function triggerSync() {
  state.syncQueue = state.syncQueue.map(q => ({ ...q, status: 'SYNCED' }));
  renderAll();
}

// Initial Load
document.addEventListener('DOMContentLoaded', () => {
  renderAll();
});
