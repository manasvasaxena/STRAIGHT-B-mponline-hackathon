/**
 * LearnQuest MP Web Edition — Fixed Form Handlers & Multi-Question Quiz Publishing
 * Powered by Google Gemini AI (API: AQ.Ab8RN6LV_QRl0q-dndA_WrQLQ0ydlhc-rDoh8i7aS1zh9zsC5g)
 */

// ─── GEMINI AI CONFIGURATION ───────────────────────────────────────────────
const GEMINI_API_KEY = 'AQ.Ab8RN6LV_QRl0q-dndA_WrQLQ0ydlhc-rDoh8i7aS1zh9zsC5g';
const GEMINI_API_URL = `https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent?key=${GEMINI_API_KEY}`;

async function callGeminiAPI(prompt) {
    try {
        const response = await fetch(GEMINI_API_URL, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({
                contents: [{ parts: [{ text: prompt }] }],
                generationConfig: { temperature: 0.7, maxOutputTokens: 2048 }
            })
        });
        if (!response.ok) throw new Error('API Error: ' + response.status);
        const data = await response.json();
        return data.candidates?.[0]?.content?.parts?.[0]?.text || null;
    } catch(err) {
        console.warn('Gemini API call failed, using local engine:', err.message);
        return null;
    }
}

// PERSISTENT LOCAL STORAGE DATABASE
function getStoredModules() {
    const saved = localStorage.getItem('lq_custom_modules_db');
    if (saved) {
        try { return JSON.parse(saved); } catch(e){}
    }
    return [];
}

function saveCustomModuleToStorage(newModule) {
    const existing = getStoredModules();
    if (!newModule.timestamp) newModule.timestamp = Date.now();
    existing.push(newModule);
    localStorage.setItem('lq_custom_modules_db', JSON.stringify(existing));
}

function formatTimestamp(ts) {
    if (!ts) return 'Published Recently';
    const date = new Date(ts);
    const now = new Date();
    const diffMs = now - date;
    const diffHours = Math.floor(diffMs / (1000 * 60 * 60));
    const diffDays = Math.floor(diffMs / (1000 * 60 * 60 * 24));

    if (diffHours < 1) return 'Published Just Now';
    if (diffHours < 24) return `Published ${diffHours} Hours Ago`;
    if (diffDays === 1) return 'Published Yesterday';
    if (diffDays <= 3) return `Published ${diffDays} Days Ago`;
    return `Published ${date.toLocaleDateString('hi-IN', { month: 'short', day: 'numeric', year: 'numeric' })}`;
}

function getStoredAssignments() {
    const saved = localStorage.getItem('lq_assignments_db');
    if (saved) {
        try { return JSON.parse(saved); } catch(e){}
    }
    return [
        {
            id: 'a1',
            grade: 'Class 8',
            subject: 'Science',
            type: 'Hands-on Project',
            title: 'प्रकाश परावर्तन नियम सत्यापन मॉडल (Project)',
            instructions: 'समतल दर्पण तथा लेज़र लाइट/टॉर्च की सहायता से आपतन कोण (∠i) तथा परावर्तन कोण (∠r) को नापकर प्रयोगात्मक रिपोर्ट तैयार करें।',
            dueDate: '2026-10-10',
            timestamp: Date.now() - 86400000,
            submitted: false
        },
        {
            id: 'a2',
            grade: 'Class 8',
            subject: 'Mathematics',
            type: 'Open Question',
            title: 'एक चर वाले रैखिक समीकरण 10 प्रश्न अभ्यास',
            instructions: 'पाठ्यपुस्तक अध्याय 2 के पृष्ठ 25 से प्रश्न 1 से 10 हल करके उत्तर प्रविष्ट करें।',
            dueDate: '2026-10-05',
            timestamp: Date.now() - 172800000,
            submitted: false
        }
    ];
}

function saveAssignmentToStorage(assignment) {
    const existing = getStoredAssignments();
    existing.unshift(assignment);
    localStorage.setItem('lq_assignments_db', JSON.stringify(existing));
}

function deleteAssignmentFromStorage(assignmentId) {
    let existing = getStoredAssignments();
    existing = existing.filter(a => a.id !== assignmentId);
    localStorage.setItem('lq_assignments_db', JSON.stringify(existing));
}

function deleteCustomModuleFromStorage(moduleId) {
    let existing = getStoredModules();
    existing = existing.filter(m => m.id !== moduleId);
    localStorage.setItem('lq_custom_modules_db', JSON.stringify(existing));
}

function getStoredQuizQuestions() {
    const saved = localStorage.getItem('lq_custom_quizzes_db');
    if (saved) {
        try { return JSON.parse(saved); } catch(e){}
    }
    return [];
}

function saveCustomQuizSetToStorage(quizSet) {
    const existing = getStoredQuizQuestions();
    existing.push(quizSet);
    localStorage.setItem('lq_custom_quizzes_db', JSON.stringify(existing));
}

// DEFAULT ROSTER OF 28 ENROLLED STUDENTS ACROSS MP SCHOOLS
const DEFAULT_ENROLLED_STUDENTS = [
    { username: 'aarav', password: 'student123', name: 'Aarav Sharma', role: 'STUDENT', grade: 'Class 8', school: 'Govt Middle School, Sehore', xp: 520, streakDays: 7, quizzesTaken: 14, avgScore: 86, assignmentsDone: 4, masteryStatus: 'Mastered', strengths: { Science: 88, Mathematics: 84, SocialScience: 90, Hindi: 85, English: 82 } },
    { username: 'priya', password: 'student123', name: 'Priya Tribal', role: 'STUDENT', grade: 'Class 7', school: 'Govt High School, Dhar', xp: 340, streakDays: 4, quizzesTaken: 11, avgScore: 74, assignmentsDone: 3, masteryStatus: 'Developing', strengths: { Science: 76, Mathematics: 68, SocialScience: 80, Hindi: 78, English: 66 } },
    { username: 'rohit', password: 'student123', name: 'Rohit Patel', role: 'STUDENT', grade: 'Class 8', school: 'Govt Excellence School, Bhopal', xp: 290, streakDays: 3, quizzesTaken: 12, avgScore: 58, assignmentsDone: 2, masteryStatus: 'Needs Help', strengths: { Science: 60, Mathematics: 52, SocialScience: 65, Hindi: 70, English: 54 } },
    { username: 'sunita', password: 'student123', name: 'Sunita Meena', role: 'STUDENT', grade: 'Class 8', school: 'Govt High School, Raisen', xp: 480, streakDays: 6, quizzesTaken: 15, avgScore: 89, assignmentsDone: 4, masteryStatus: 'Mastered', strengths: { Science: 92, Mathematics: 85, SocialScience: 88, Hindi: 90, English: 84 } },
    { username: 'deepak', password: 'student123', name: 'Deepak Malviya', role: 'STUDENT', grade: 'Class 8', school: 'Govt Model School, Hoshangabad', xp: 360, streakDays: 5, quizzesTaken: 10, avgScore: 72, assignmentsDone: 3, masteryStatus: 'Developing', strengths: { Science: 70, Mathematics: 68, SocialScience: 75, Hindi: 80, English: 67 } },
    { username: 'anjali', password: 'student123', name: 'Anjali Verma', role: 'STUDENT', grade: 'Class 8', school: 'Govt Girls School, Vidisha', xp: 550, streakDays: 8, quizzesTaken: 16, avgScore: 92, assignmentsDone: 4, masteryStatus: 'Mastered', strengths: { Science: 94, Mathematics: 90, SocialScience: 92, Hindi: 95, English: 89 } },
    { username: 'manoj', password: 'student123', name: 'Manoj Rathore', role: 'STUDENT', grade: 'Class 8', school: 'Govt School, Ujjain', xp: 280, streakDays: 3, quizzesTaken: 9, avgScore: 64, assignmentsDone: 2, masteryStatus: 'Developing', strengths: { Science: 62, Mathematics: 60, SocialScience: 68, Hindi: 72, English: 58 } },
    { username: 'sanjay', password: 'student123', name: 'Sanjay Baghel', role: 'STUDENT', grade: 'Class 8', school: 'Govt Excellence School, Gwalior', xp: 410, streakDays: 5, quizzesTaken: 13, avgScore: 78, assignmentsDone: 3, masteryStatus: 'Developing', strengths: { Science: 80, Mathematics: 75, SocialScience: 82, Hindi: 80, English: 72 } },
    { username: 'ritu', password: 'student123', name: 'Ritu Lodhi', role: 'STUDENT', grade: 'Class 8', school: 'Govt High School, Sagar', xp: 460, streakDays: 6, quizzesTaken: 14, avgScore: 84, assignmentsDone: 4, masteryStatus: 'Developing', strengths: { Science: 85, Mathematics: 80, SocialScience: 86, Hindi: 88, English: 81 } },
    { username: 'ajay', password: 'student123', name: 'Ajay Prajapati', role: 'STUDENT', grade: 'Class 8', school: 'Govt Middle School, Chhatarpur', xp: 230, streakDays: 2, quizzesTaken: 8, avgScore: 52, assignmentsDone: 2, masteryStatus: 'Needs Help', strengths: { Science: 54, Mathematics: 48, SocialScience: 56, Hindi: 62, English: 44 } },
    { username: 'jyoti', password: 'student123', name: 'Jyoti Ahirwar', role: 'STUDENT', grade: 'Class 8', school: 'Govt School, Tikamgarh', xp: 390, streakDays: 4, quizzesTaken: 11, avgScore: 76, assignmentsDone: 3, masteryStatus: 'Developing', strengths: { Science: 78, Mathematics: 72, SocialScience: 79, Hindi: 82, English: 70 } },
    { username: 'suman', password: 'student123', name: 'Suman Sahariya', role: 'STUDENT', grade: 'Class 8', school: 'Govt High School, Shivpuri', xp: 320, streakDays: 3, quizzesTaken: 10, avgScore: 68, assignmentsDone: 3, masteryStatus: 'Developing', strengths: { Science: 66, Mathematics: 62, SocialScience: 74, Hindi: 75, English: 60 } },
    { username: 'vandana', password: 'student123', name: 'Vandana Soni', role: 'STUDENT', grade: 'Class 8', school: 'Govt Model School, Jabalpur', xp: 510, streakDays: 7, quizzesTaken: 15, avgScore: 90, assignmentsDone: 4, masteryStatus: 'Mastered', strengths: { Science: 91, Mathematics: 88, SocialScience: 92, Hindi: 93, English: 86 } },
    { username: 'preeti', password: 'student123', name: 'Preeti Kevat', role: 'STUDENT', grade: 'Class 8', school: 'Govt School, Shahdol', xp: 430, streakDays: 5, quizzesTaken: 12, avgScore: 82, assignmentsDone: 3, masteryStatus: 'Developing', strengths: { Science: 84, Mathematics: 78, SocialScience: 85, Hindi: 86, English: 77 } },
    { username: 'divya', password: 'student123', name: 'Divya Sahu', role: 'STUDENT', grade: 'Class 8', school: 'Govt Girls School, Narsinghpur', xp: 470, streakDays: 6, quizzesTaken: 13, avgScore: 85, assignmentsDone: 4, masteryStatus: 'Mastered', strengths: { Science: 86, Mathematics: 82, SocialScience: 88, Hindi: 89, English: 81 } },
    { username: 'hemant', password: 'student123', name: 'Hemant Bhilala', role: 'STUDENT', grade: 'Class 8', school: 'Govt Middle School, Alirajpur', xp: 240, streakDays: 2, quizzesTaken: 7, avgScore: 55, assignmentsDone: 2, masteryStatus: 'Needs Help', strengths: { Science: 56, Mathematics: 50, SocialScience: 60, Hindi: 64, English: 46 } },
    { username: 'vikas', password: 'student123', name: 'Vikas Gond', role: 'STUDENT', grade: 'Class 7', school: 'Govt High School, Mandla', xp: 330, streakDays: 4, quizzesTaken: 9, avgScore: 70, assignmentsDone: 2, masteryStatus: 'Developing', strengths: { Science: 72, Mathematics: 65, SocialScience: 74, Hindi: 76, English: 64 } },
    { username: 'kavita', password: 'student123', name: 'Kavita Chouhan', role: 'STUDENT', grade: 'Class 7', school: 'Govt School, Barwani', xp: 420, streakDays: 5, quizzesTaken: 12, avgScore: 81, assignmentsDone: 3, masteryStatus: 'Developing', strengths: { Science: 83, Mathematics: 78, SocialScience: 84, Hindi: 86, English: 75 } },
    { username: 'amit', password: 'student123', name: 'Amit Vishwakarma', role: 'STUDENT', grade: 'Class 7', school: 'Govt Middle School, Damoh', xp: 370, streakDays: 4, quizzesTaken: 10, avgScore: 75, assignmentsDone: 3, masteryStatus: 'Developing', strengths: { Science: 76, Mathematics: 72, SocialScience: 78, Hindi: 80, English: 70 } },
    { username: 'mohit', password: 'student123', name: 'Mohit Parihar', role: 'STUDENT', grade: 'Class 7', school: 'Govt High School, Balaghat', xp: 400, streakDays: 5, quizzesTaken: 11, avgScore: 79, assignmentsDone: 3, masteryStatus: 'Developing', strengths: { Science: 80, Mathematics: 76, SocialScience: 82, Hindi: 84, English: 74 } },
    { username: 'rahul', password: 'student123', name: 'Rahul Bhil', role: 'STUDENT', grade: 'Class 6', school: 'Govt Middle School, Jhabua', xp: 310, streakDays: 3, quizzesTaken: 8, avgScore: 69, assignmentsDone: 2, masteryStatus: 'Developing', strengths: { Science: 70, Mathematics: 64, SocialScience: 72, Hindi: 74, English: 62 } },
    { username: 'meena_k', password: 'student123', name: 'Meena Kushwaha', role: 'STUDENT', grade: 'Class 6', school: 'Govt School, Satna', xp: 440, streakDays: 6, quizzesTaken: 10, avgScore: 84, assignmentsDone: 3, masteryStatus: 'Developing', strengths: { Science: 85, Mathematics: 80, SocialScience: 86, Hindi: 88, English: 80 } },
    { username: 'karan', password: 'student123', name: 'Karan Korku', role: 'STUDENT', grade: 'Class 6', school: 'Govt High School, Betul', xp: 350, streakDays: 4, quizzesTaken: 9, avgScore: 73, assignmentsDone: 2, masteryStatus: 'Developing', strengths: { Science: 74, Mathematics: 68, SocialScience: 76, Hindi: 78, English: 66 } },
    { username: 'neha', password: 'student123', name: 'Neha Solanki', role: 'STUDENT', grade: 'Class 9', school: 'Govt High School, Khargone', xp: 490, streakDays: 7, quizzesTaken: 14, avgScore: 88, assignmentsDone: 4, masteryStatus: 'Mastered', strengths: { Science: 90, Mathematics: 85, SocialScience: 90, Hindi: 92, English: 84 } },
    { username: 'sachin', password: 'student123', name: 'Sachin Sen', role: 'STUDENT', grade: 'Class 9', school: 'Govt Excellence School, Rewa', xp: 390, streakDays: 5, quizzesTaken: 12, avgScore: 77, assignmentsDone: 3, masteryStatus: 'Developing', strengths: { Science: 78, Mathematics: 74, SocialScience: 80, Hindi: 82, English: 72 } },
    { username: 'rakesh', password: 'student123', name: 'Rakesh Rawat', role: 'STUDENT', grade: 'Class 9', school: 'Govt Model School, Morena', xp: 300, streakDays: 3, quizzesTaken: 11, avgScore: 66, assignmentsDone: 3, masteryStatus: 'Developing', strengths: { Science: 68, Mathematics: 62, SocialScience: 70, Hindi: 72, English: 60 } },
    { username: 'pooja', password: 'student123', name: 'Pooja Yadav', role: 'STUDENT', grade: 'Class 10', school: 'Govt Excellence School, Dewas', xp: 620, streakDays: 10, quizzesTaken: 18, avgScore: 94, assignmentsDone: 5, masteryStatus: 'Mastered', strengths: { Science: 96, Mathematics: 92, SocialScience: 95, Hindi: 96, English: 90 } },
    { username: 'priyanka', password: 'student123', name: 'Priyanka Tomar', role: 'STUDENT', grade: 'Class 10', school: 'Govt Girls High School, Bhind', xp: 580, streakDays: 9, quizzesTaken: 16, avgScore: 91, assignmentsDone: 5, masteryStatus: 'Mastered', strengths: { Science: 93, Mathematics: 89, SocialScience: 92, Hindi: 94, English: 87 } }
];

function getStoredUsers() {
    const saved = localStorage.getItem('lq_users_db');
    if (saved) {
        try { return JSON.parse(saved); } catch(e){}
    }
    return DEFAULT_ENROLLED_STUDENTS.map(s => ({
        username: s.username,
        password: s.password,
        name: s.name,
        role: s.role,
        grade: s.grade,
        school: s.school,
        xp: s.xp,
        streakDays: s.streakDays
    }));
}

function getEnrolledStudentsRoster() {
    const customUsers = getStoredUsers().filter(u => u.role === 'STUDENT');
    const rosterMap = new Map();
    DEFAULT_ENROLLED_STUDENTS.forEach(s => rosterMap.set(s.username.toLowerCase(), { ...s }));
    customUsers.forEach(u => {
        const key = u.username.toLowerCase();
        if (rosterMap.has(key)) {
            const existing = rosterMap.get(key);
            existing.xp = u.xp || existing.xp;
            existing.streakDays = u.streakDays || existing.streakDays;
            existing.grade = u.grade || existing.grade;
            existing.school = u.school || existing.school;
            existing.name = u.name || existing.name;
        } else {
            rosterMap.set(key, {
                username: u.username,
                password: u.password || 'student123',
                name: u.name || u.username,
                role: 'STUDENT',
                grade: u.grade || 'Class 8',
                school: u.school || 'Govt School, MP',
                xp: u.xp || 100,
                streakDays: u.streakDays || 1,
                quizzesTaken: 1,
                avgScore: 75,
                assignmentsDone: 1,
                masteryStatus: 'Developing',
                strengths: { Science: 75, Mathematics: 70, SocialScience: 78, Hindi: 80, English: 72 }
            });
        }
    });
    return Array.from(rosterMap.values());
}

function saveUsersToStorage(users) {
    localStorage.setItem('lq_users_db', JSON.stringify(users));
}

function getStoredDoubts() {
    const saved = localStorage.getItem('lq_doubts_db');
    if (saved) {
        try { return JSON.parse(saved); } catch(e){}
    }
    return [
        {
            id: 'd1',
            studentName: 'Aarav Sharma',
            studentId: 'aarav',
            grade: 'Class 8',
            school: 'Govt Middle School, Sehore',
            questionText: 'प्रकाश का परावर्तन क्या है? (What is Reflection?)',
            timestamp: Date.now() - 3600000 * 2,
            isEscalatedToTeacher: true,
            teacherReply: 'आरव, जब प्रकाश किसी चमकदार दर्पण से टकराकर लौटता है तो इसे परावर्तन कहते हैं। नियम: आपतन कोण = परावर्तन कोण (∠i = ∠r)।',
            repliedAt: Date.now() - 3600000
        },
        {
            id: 'd2',
            studentName: 'Priya Tribal',
            studentId: 'priya',
            grade: 'Class 7',
            school: 'Govt High School, Dhar',
            questionText: 'बीजगणित में समीकरण 2x + 4 = 10 में "x" का मान कैसे निकालते हैं?',
            timestamp: Date.now() - 3600000 * 5,
            isEscalatedToTeacher: true,
            teacherReply: 'प्रिया, पहले दाएँ पक्ष में 4 घटाएँ (2x = 10 - 4 = 6), फिर 2 से भाग दें (x = 6/2 = 3)।',
            repliedAt: Date.now() - 3600000 * 3
        },
        {
            id: 'd3',
            studentName: 'Rohit Patel',
            studentId: 'rohit',
            grade: 'Class 8',
            school: 'Govt Excellence School, Bhopal',
            questionText: 'पौधों में प्रकाश संश्लेषण के लिए रंध्र (Stomata) द्वारा कार्बन डाइऑक्साइड कैसे ली जाती है?',
            timestamp: Date.now() - 3600000 * 1,
            isEscalatedToTeacher: true,
            teacherReply: null,
            repliedAt: null
        }
    ];
}

function saveDoubtsToStorage(doubts) {
    localStorage.setItem('lq_doubts_db', JSON.stringify(doubts));
}

// PERSISTENT ASSIGNMENT SUBMISSIONS & GRADING DATABASE
function getStoredSubmissions() {
    const saved = localStorage.getItem('lq_submissions_db');
    if (saved) {
        try { return JSON.parse(saved); } catch(e){}
    }
    return [
        {
            id: 'sub_1',
            assignmentId: 'a1',
            assignmentTitle: 'प्रकाश परावर्तन नियम सत्यापन मॉडल (Project)',
            studentId: 'aarav',
            studentName: 'Aarav Sharma',
            grade: 'Class 8',
            school: 'Govt Middle School, Sehore',
            submittedAt: Date.now() - 14400000,
            content: 'मैंने समतल दर्पण और लेजर टॉर्च का उपयोग करके आपतन कोण 30° और परावर्तन कोण 30° मापा। दोनों कोण बराबर पाए गए (∠i = ∠r = 30°)। अभिलंब तथा आपतित और परावर्तित किरणें तीनों एक ही तल में स्थित हैं। मॉडल की चार्ट शीट तैयार कर ली है।',
            attachmentNote: 'Science Project Model Verified by Village School Headmaster',
            status: 'GRADED',
            marks: 9,
            maxMarks: 10,
            gradeLetter: 'A',
            teacherRemarks: 'शाबाश आरव! आपतन और परावर्तन के दोनों नियमों को प्रयोगात्मक रूप से सिद्ध किया है। रेखाचित्र और प्रयोग विधि बहुत स्पष्ट है।',
            gradedBy: 'Shri S.K. Verma',
            gradedAt: Date.now() - 3600000
        },
        {
            id: 'sub_2',
            assignmentId: 'a1',
            assignmentTitle: 'प्रकाश परावर्तन नियम सत्यापन मॉडल (Project)',
            studentId: 'priya',
            studentName: 'Priya Tribal',
            grade: 'Class 7',
            school: 'Govt High School, Dhar',
            submittedAt: Date.now() - 7200000,
            content: 'समतल दर्पण पर टॉर्च से प्रकाश डाला। कोण चांदे (protractor) से 45 अंश मापा गया। परावर्तित किरण भी 45 अंश पर निकली। आपतन कोण और परावर्तन कोण समान होते हैं।',
            attachmentNote: 'Cardboard Reflection Board Setup',
            status: 'PENDING',
            marks: null,
            maxMarks: 10,
            gradeLetter: '',
            teacherRemarks: '',
            gradedBy: '',
            gradedAt: null
        },
        {
            id: 'sub_3',
            assignmentId: 'a2',
            assignmentTitle: 'एक चर वाले रैखिक समीकरण 10 प्रश्न अभ्यास',
            studentId: 'rohit',
            studentName: 'Rohit Patel',
            grade: 'Class 8',
            school: 'Govt Excellence School, Bhopal',
            submittedAt: Date.now() - 10800000,
            content: 'प्रश्न 1: 3x - 5 = 10 ➔ 3x = 15 ➔ x = 5। प्रश्न 2: 2y + 8 = 20 ➔ 2y = 12 ➔ y = 6। प्रश्न 3: 5x + 3 = 2x + 15 ➔ 3x = 12 ➔ x = 4। प्रश्न 4 से 10 तक सभी हल रफ कॉपी में करके उत्तर प्रविष्ट किए हैं।',
            attachmentNote: 'Class 8 Chapter 2 Linear Equations Notebook Pg 25',
            status: 'PENDING',
            marks: null,
            maxMarks: 10,
            gradeLetter: '',
            teacherRemarks: '',
            gradedBy: '',
            gradedAt: null
        },
        {
            id: 'sub_4',
            assignmentId: 'a1',
            assignmentTitle: 'प्रकाश परावर्तन नियम सत्यापन मॉडल (Project)',
            studentId: 'sunita',
            studentName: 'Sunita Meena',
            grade: 'Class 8',
            school: 'Govt High School, Raisen',
            submittedAt: Date.now() - 18000000,
            content: 'कार्डबोर्ड पर सफेद कागज चिपकाकर अभिलंब खींचा। 40° पर लेजर बीम डाली, परावर्तित बीम ठीक 40° पर प्राप्त हुई। ∠i = ∠r सत्यापित हुआ।',
            attachmentNote: 'Light Ray Model Chart with Colored Pins',
            status: 'GRADED',
            marks: 8,
            maxMarks: 10,
            gradeLetter: 'B+',
            teacherRemarks: 'सुनीता, बहुत अच्छा प्रयास। दर्पण की सतह को बिल्कुल लंबवत रखना आवश्यक है। प्रयोग सारणी बहुत सुंदर बनी है।',
            gradedBy: 'Shri S.K. Verma',
            gradedAt: Date.now() - 7200000
        }
    ];
}

function saveSubmissionsToStorage(subs) {
    localStorage.setItem('lq_submissions_db', JSON.stringify(subs));
}

// PERSISTENT QUIZ ANALYTICS STORAGE
function getStoredQuizAnalytics() {
    const saved = localStorage.getItem('lq_quiz_analytics_db');
    if (saved) {
        try { return JSON.parse(saved); } catch(e){}
    }
    return [
        {
            id: 'qa_1',
            subject: 'Science',
            grade: 'Class 8',
            questionText: 'प्रकाश की गति निर्वात में कितनी होती है?',
            correctAnswer: '3 x 10^8 m/s',
            totalResponses: 28,
            breakdown: {
                'Option A: 3 x 10^8 m/s': { count: 23, pct: '82%', isCorrect: true },
                'Option B: 3 x 10^6 m/s': { count: 3, pct: '11%', isCorrect: false },
                'Option C: 3000 m/s': { count: 1, pct: '4%', isCorrect: false },
                'Option D: 300 km/s': { count: 1, pct: '3%', isCorrect: false }
            }
        },
        {
            id: 'qa_2',
            subject: 'Science',
            grade: 'Class 8',
            questionText: 'पौधों में पत्तियों का हरा रंग किस वर्णक के कारण होता है?',
            correctAnswer: 'क्लोरोफिल (Chlorophyll)',
            totalResponses: 28,
            breakdown: {
                'Option A: क्लोरोफिल (Chlorophyll)': { count: 25, pct: '89%', isCorrect: true },
                'Option B: कैरोटीन': { count: 2, pct: '7%', isCorrect: false },
                'Option C: ऐंथोसायनिन': { count: 1, pct: '4%', isCorrect: false }
            }
        },
        {
            id: 'qa_3',
            subject: 'Mathematics',
            grade: 'Class 8',
            questionText: 'यदि 3x - 9 = 15 हो, तो x का मान क्या होगा?',
            correctAnswer: '8',
            totalResponses: 28,
            breakdown: {
                'Option A: 8': { count: 18, pct: '64%', isCorrect: true },
                'Option B: 6': { count: 5, pct: '18%', isCorrect: false },
                'Option C: 7': { count: 3, pct: '11%', isCorrect: false },
                'Option D: 5': { count: 2, pct: '7%', isCorrect: false }
            }
        },
        {
            id: 'qa_4',
            subject: 'Mathematics',
            grade: 'Class 8',
            questionText: 'त्रिभुज के तीनों आंतरिक कोणों का योग कितना होता है?',
            correctAnswer: '180°',
            totalResponses: 28,
            breakdown: {
                'Option A: 180°': { count: 26, pct: '93%', isCorrect: true },
                'Option B: 360°': { count: 2, pct: '7%', isCorrect: false }
            }
        }
    ];
}

function saveQuizAnalyticsToStorage(analytics) {
    localStorage.setItem('lq_quiz_analytics_db', JSON.stringify(analytics));
}

function recordQuizAnswerToAnalytics(subject, question, selectedIndex) {
    const list = getStoredQuizAnalytics();
    let item = list.find(qa => qa.questionText.trim() === question.questionText.trim());
    if (!item) {
        item = {
            id: 'qa_' + Date.now(),
            subject: subject || 'Science',
            grade: 'Class 8',
            questionText: question.questionText,
            correctAnswer: question.options[question.correctAnswerIndex],
            totalResponses: 0,
            breakdown: {}
        };
        question.options.forEach((opt, idx) => {
            const label = `Option ${String.fromCharCode(65 + idx)}: ${opt}`;
            item.breakdown[label] = { count: 0, pct: '0%', isCorrect: idx === question.correctAnswerIndex };
        });
        list.push(item);
    }

    item.totalResponses = (item.totalResponses || 0) + 1;
    const selectedKey = Object.keys(item.breakdown)[selectedIndex];
    if (selectedKey && item.breakdown[selectedKey]) {
        item.breakdown[selectedKey].count = (item.breakdown[selectedKey].count || 0) + 1;
    }

    // Recompute percentages
    Object.keys(item.breakdown).forEach(k => {
        const c = item.breakdown[k].count || 0;
        const p = Math.round((c / item.totalResponses) * 100);
        item.breakdown[k].pct = `${p}%`;
    });

    saveQuizAnalyticsToStorage(list);
    state.studentQuizAnalytics = list;
}


const state = {
    activeTab: 'ai-solver',
    currentUser: JSON.parse(localStorage.getItem('lq_current_user')) || {
        username: 'aarav',
        name: 'Aarav Sharma',
        role: 'STUDENT',
        grade: 'Class 8',
        school: 'Govt Middle School, Sehore',
        xp: 520,
        streakDays: 7
    },
    
    // VERIFIED TEACHERS DATABASE
    teacherDatabase: [
        { teacherId: 'MP-TEACHER-101', name: 'Shri S.K. Verma', password: 'teacher123', school: 'Govt Excellence School, Bhopal', subject: 'Science & Math' },
        { teacherId: 'MP-TEACHER-102', name: 'Smt. Anita Sharma', password: 'teacher123', school: 'Govt Model School, Indore', subject: 'Social Science & Hindi' },
        { teacherId: 'MP-TEACHER-103', name: 'Dr. Rajesh Patel', password: 'teacher123', school: 'Govt High School, Jabalpur', subject: 'English & Computer' }
    ],

    selectedModuleGrade: 'Class 8',
    selectedModuleSubject: 'All',
    selectedAssignmentGrade: 'Class 8',
    selectedAssignmentSubject: 'All',
    selectedQuizSubject: 'Science',

    // EXPANDED MP BOARD CURRICULUM MODULES (CLASS 6-10 ALL SUBJECTS WITH TIMESTAMPS)
    mpBoardModules: [
        // CLASS 8
        { id: 'm8_sci', grade: 'Class 8', subject: 'Science', timestamp: Date.now() - 3600000 * 12, titleHindi: 'कक्षा 8 विज्ञान: प्रकाश एवं परावर्तन (MPBSE Ch 16)', titleEnglish: 'Class 8 Science: Light & Reflection', lessons: [{ title: '1. प्रकाश के गुण', content: 'प्रकाश सीधी रेखा में गति करता है। निर्वात में इसकी चाल 3 x 10^8 m/s है।' }, { title: '2. परावर्तन के नियम', content: 'आपतन कोण (∠i) = परावर्तन कोण (∠r)।' }] },
        { id: 'm8_math', grade: 'Class 8', subject: 'Mathematics', timestamp: Date.now() - 3600000 * 28, titleHindi: 'कक्षा 8 गणित: एक चर वाले रैखिक समीकरण (MPBSE Ch 2)', titleEnglish: 'Class 8 Math: Linear Equations', lessons: [{ title: '1. समीकरण हल करना', content: 'पक्षांतरण द्वारा चर x का मान प्राप्त करते हैं। जैसे 2x + 5 = 15 ➔ x = 5।' }] },
        { id: 'm8_sst', grade: 'Class 8', subject: 'Social Science', timestamp: Date.now() - 3600000 * 50, titleHindi: 'कक्षा 8 सामाजिक विज्ञान: हमारा संविधान एवं MP इतिहास', titleEnglish: 'Class 8 Social Science: MP History & Civics', lessons: [{ title: '1. सांची का स्तूप', content: 'सांची का स्तूप सम्राट अशोक द्वारा निर्मित बौद्ध स्मारक है जो रायसेन जिले में स्थित है।' }] },
        { id: 'm8_hin', grade: 'Class 8', subject: 'Hindi', timestamp: Date.now() - 3600000 * 70, titleHindi: 'कक्षा 8 हिन्दी: सुगम भारती, भाषा, संधि एवं समास', titleEnglish: 'Class 8 Hindi: Grammar & Literature', lessons: [{ title: '1. संधि के प्रकार', content: 'हिम + आलय = हिमालय (दीर्घ स्वर संधि)।' }] },
        { id: 'm8_eng', grade: 'Class 8', subject: 'English', timestamp: Date.now() - 3600000 * 90, titleHindi: 'कक्षा 8 अंग्रेज़ी: Parts of Speech & Tenses', titleEnglish: 'Class 8 English Grammar', lessons: [{ title: '1. Nouns and Verbs', content: 'A Noun is a naming word. A Verb represents an action.' }] },

        // CLASS 7
        { id: 'm7_sci', grade: 'Class 7', subject: 'Science', timestamp: Date.now() - 3600000 * 10, titleHindi: 'कक्षा 7 विज्ञान: पादपों में पोषण (Photosynthesis)', titleEnglish: 'Class 7 Science: Plant Nutrition', lessons: [{ title: '1. प्रकाश संश्लेषण', content: 'हरे पौधे सूर्य प्रकाश व क्लोरोफिल द्वारा CO2 और जल से ग्लूकोज बनाते हैं।' }] },
        { id: 'm7_math', grade: 'Class 7', subject: 'Mathematics', timestamp: Date.now() - 3600000 * 30, titleHindi: 'कक्षा 7 गणित: भिन्न एवं दशमलव (Fractions)', titleEnglish: 'Class 7 Math: Fractions', lessons: [{ title: '1. भिन्नों का जोड़ व गुणा', content: 'समान हर वाली भिन्नों के अंशों को सीधे जोड़ दिया जाता है।' }] },

        // CLASS 6
        { id: 'm6_sci', grade: 'Class 6', subject: 'Science', timestamp: Date.now() - 3600000 * 15, titleHindi: 'कक्षा 6 विज्ञान: भोजन के घटक एवं सजीव जगत', titleEnglish: 'Class 6 Science: Food & Living Organisms', lessons: [{ title: '1. भोजन के मुख्य पोषक तत्व', content: 'कार्बोहाइड्रेट, प्रोटीन, वसा, विटामिन C और खनिज लवण।' }] },

        // CLASS 9
        { id: 'm9_sci', grade: 'Class 9', subject: 'Science', timestamp: Date.now() - 3600000 * 20, titleHindi: 'कक्षा 9 विज्ञान: बल तथा गति के नियम (Newton Laws)', titleEnglish: 'Class 9 Science: Force & Motion', lessons: [{ title: '1. न्यूटन के गति नियम', content: 'प्रथम नियम (जड़त्व नियम): विराम में रखी वस्तु बाह्य बल बिना नहीं चलती।' }] },

        // CLASS 10
        { id: 'm10_sci', grade: 'Class 10', subject: 'Science', timestamp: Date.now() - 3600000 * 25, titleHindi: 'कक्षा 10 विज्ञान: रासायनिक अभिक्रियाएँ व अम्ल-क्षार', titleEnglish: 'Class 10 Science: Chemical Reactions', lessons: [{ title: '1. रासायनिक समीकरण संतुलित करना', content: 'द्रव्यमान संरक्षण नियम के अनुसार दोनों तरफ परमाणुओं की संख्या समान होनी चाहिए।' }] }
    ],

    subjectQuizBank: {
        'Science': [
            { questionText: 'प्रकाश की गति निर्वात में कितनी होती है?', options: ['3 x 10^8 m/s', '3 x 10^6 m/s', '3000 m/s', '300 km/s'], correctAnswerIndex: 0, explanation: 'प्रकाश की गति निर्वात में 3 x 10^8 m/s होती है।' },
            { questionText: 'पौधों में पत्तियों का हरा रंग किस वर्णक के कारण होता है?', options: ['क्लोरोफिल (Chlorophyll)', 'कैरोटीन', 'ऐंथोसायनिन', 'ज़ैंथोफिल'], correctAnswerIndex: 0, explanation: 'क्लोरोफिल पत्तियों में सूर्य के प्रकाश को अवशोषित करता है।' }
        ],
        'Mathematics': [
            { questionText: 'यदि 3x - 9 = 15 हो, तो x का मान क्या होगा?', options: ['8', '6', '7', '5'], correctAnswerIndex: 0, explanation: '3x = 15 + 9 ➔ 3x = 24 ➔ x = 8.' },
            { questionText: 'त्रिभुज के तीनों आंतरिक कोणों का योग कितना होता है?', options: ['180°', '360°', '90°', '270°'], correctAnswerIndex: 0, explanation: 'त्रिभुज के तीनों कोणों का योग 180 अंश होता है।' }
        ]
    },

    studentQuizAnalytics: getStoredQuizAnalytics(),
    doubtsList: getStoredDoubts(),
    submissionsList: getStoredSubmissions(),

    inboxFilterStatus: 'ALL',
    inboxGradeFilter: 'All',
    inboxSearchQuery: '',

    analyticsGradeFilter: 'Class 8',
    analyticsSubjectFilter: 'All',
    analyticsRosterSearch: '',

    gradingAssignmentFilter: 'All',
    gradingStatusFilter: 'PENDING',
    gradingClassFilter: 'Class 8',


    scholarships: [
        { title: 'MP Tribal Welfare Post-Matric Scholarship', provider: 'Government of MP', category: 'Tribal / ST', eligibility: 'Class 9-12 ST students in MP', deadline: '31 Oct 2026', amount: '₹8,000 / year', isVerified: true, lastVerifiedDate: '2026-09-25' },
        { title: 'MP Mukhyamantri Medhavi Chhatra Yojana (MMVY)', provider: 'Government of MP', category: 'Merit Cum Means', eligibility: 'Class 10/12 score ≥ 70% (MP Board)', deadline: '15 Nov 2026', amount: 'Full Fee Waiver + ₹25,000', isVerified: true, lastVerifiedDate: '2026-09-26' },
        { title: 'MP OBC Post-Matric Scholarship Scheme', provider: 'OBC & Minority Welfare Dept MP', category: 'OBC Category', eligibility: 'Class 9-12 OBC students with income < 3 Lakhs', deadline: '20 Nov 2026', amount: '₹6,500 / year', isVerified: true, lastVerifiedDate: '2026-09-26' },
        { title: 'MP SC Post-Matric Scholarship Scheme', provider: 'Anusuchit Jati Kalyan Dept MP', category: 'Scheduled Caste', eligibility: 'Class 9-12 SC students in MP', deadline: '25 Nov 2026', amount: '₹8,000 / year', isVerified: true, lastVerifiedDate: '2026-09-27' },
        { title: 'Rural Science Talent Search Scheme', provider: 'MP Education Dept', category: 'Science Talent', eligibility: 'Score ≥ 75% in Class 8 Science', deadline: '30 Nov 2026', amount: '₹12,000 / year', isVerified: true, lastVerifiedDate: '2026-09-27' },
        { title: 'National Means Cum Merit Scholarship (NMMS MP)', provider: 'Ministry of Education, India', category: 'National Merit', eligibility: 'Class 8 students with income < 3.5 Lakhs', deadline: '15 Dec 2026', amount: '₹12,000 / year', isVerified: true, lastVerifiedDate: '2026-09-28' },
        { title: 'MP Pratibha Kiran Scholarship Scheme', provider: 'MP Higher Education Dept', category: 'Girls Education', eligibility: 'Urban BPL Girl Students in MP', deadline: '31 Dec 2026', amount: '₹5,000 / year', isVerified: true, lastVerifiedDate: '2026-09-28' },
        { title: 'MP Gaon Ki Beti Scheme', provider: 'School Education Dept MP', category: 'Rural Girls Merit', eligibility: 'Rural Girl Students with 1st Division in Class 12', deadline: '10 Jan 2027', amount: '₹5,000 / year (₹500/month)', isVerified: true, lastVerifiedDate: '2026-09-29' },
        { title: 'MP Free Laptop Grant Scheme for Toppers', provider: 'Government of MP', category: 'Meritorious Grant', eligibility: 'Score ≥ 75% in Class 10/12 Board Exams', deadline: '15 Jan 2027', amount: '₹25,000 One-time Laptop Grant', isVerified: true, lastVerifiedDate: '2026-09-29' }
    ],

    careers: [
        { title: 'Agriculture Extension Officer', sector: 'Government / Agri-tech', requiredEducation: 'B.Sc Agriculture', description: 'Help farmers adopt modern sustainable agricultural practices and crop protection across rural MP.', recommendedSubjects: ['Science', 'Biology', 'Environment'] },
        { title: 'Solar Microgrid & Renewable Energy Technician', sector: 'Renewable Energy', requiredEducation: 'ITI / Diploma Electrical', description: 'Install, service, and maintain solar micro-grids and off-grid power setups in rural MP villages.', recommendedSubjects: ['Physics', 'Mathematics'] },
        { title: 'MP Online Kiosk & e-Governance Specialist', sector: 'IT & MP Services', requiredEducation: '12th Pass + DCA / PGDCA', description: 'Operate citizen services, scholarship portals, and digital MP Online kiosks.', recommendedSubjects: ['Computer Science', 'English', 'Mathematics'] },
        { title: 'Rural Health Worker & Nursing Assistant', sector: 'Healthcare', requiredEducation: 'ANM / B.Sc Nursing', description: 'Provide primary healthcare, vaccination awareness, and maternal care in rural MP health centers.', recommendedSubjects: ['Biology', 'Chemistry'] },
        { title: 'Veterinary Extension Assistant (Pashu Chikitsa)', sector: 'Animal Husbandry', requiredEducation: 'Diploma in Animal Husbandry', description: 'Assist livestock farmers with animal vaccination, breeding, and dairy health care.', recommendedSubjects: ['Biology', 'Science'] },
        { title: 'Soil Health Analyst & Krishi Vigyan Consultant', sector: 'Agri-Science', requiredEducation: 'B.Sc Chemistry / Agriculture', description: 'Test soil NPK levels and advise MP farmers on optimal fertilizer usage.', recommendedSubjects: ['Chemistry', 'Biology'] },
        { title: 'Agri-Drone Pilot & Precision Agriculture Tech', sector: 'Agri-Tech & Drones', requiredEducation: '12th Pass + Drone Pilot License', description: 'Operate agricultural spraying drones for large-scale crop monitoring and pesticide spraying in MP fields.', recommendedSubjects: ['Physics', 'Computer Science'] },
        { title: 'Gram Panchayat Digital Rozgar Sahayak', sector: 'Panchayati Raj MP', requiredEducation: '12th Pass + Computer Basics', description: 'Manage digital MGNREGA records, job cards, and Panchayat development projects.', recommendedSubjects: ['Social Science', 'Computer Science'] }
    ],

    currentAISolution: null,
    teacherQuestionCount: 1,
    generatedTeacherAIModule: null
};

// INITIALIZATION
document.addEventListener('DOMContentLoaded', () => {
    initEvents();
    renderAll();
});

function initEvents() {
    // Auth Modal events
    const openLoginBtn = document.getElementById('open-login-btn');
    const loginModal = document.getElementById('login-modal');
    const closeLoginModal = document.getElementById('close-login-modal');

    if (openLoginBtn && loginModal) {
        openLoginBtn.addEventListener('click', () => loginModal.classList.remove('hidden'));
    }
    
    if (closeLoginModal && loginModal) {
        closeLoginModal.addEventListener('click', () => loginModal.classList.add('hidden'));
        loginModal.addEventListener('click', (e) => {
            if (e.target === loginModal) loginModal.classList.add('hidden');
        });
    }

    const lessonModal = document.getElementById('lesson-modal');
    const closeModal = document.getElementById('close-modal');
    if (closeModal && lessonModal) {
        closeModal.addEventListener('click', () => lessonModal.classList.add('hidden'));
        lessonModal.addEventListener('click', (e) => {
            if (e.target === lessonModal) lessonModal.classList.add('hidden');
        });
    }

    // Role switcher in auth modal
    const tabStudent = document.getElementById('login-tab-student');
    const tabTeacher = document.getElementById('login-tab-teacher');
    const studentForm = document.getElementById('student-auth-form');
    const teacherForm = document.getElementById('teacher-auth-form');

    if (tabStudent && tabTeacher && studentForm && teacherForm) {
        tabStudent.addEventListener('click', () => {
            tabStudent.classList.add('active');
            tabTeacher.classList.remove('active');
            studentForm.classList.remove('hidden');
            teacherForm.classList.add('hidden');
        });

        tabTeacher.addEventListener('click', () => {
            tabTeacher.classList.add('active');
            tabStudent.classList.remove('active');
            teacherForm.classList.remove('hidden');
            studentForm.classList.add('hidden');
        });
    }

    // Student Login / Register Submit
    if (studentForm) {
        studentForm.addEventListener('submit', (e) => {
            e.preventDefault();
            const username = document.getElementById('student-name-input').value.trim();
            const password = document.getElementById('student-pass-input').value.trim();
            const grade = document.getElementById('student-grade-select').value;
            const school = document.getElementById('student-school-input').value.trim();

            let usersDB = getStoredUsers();
            let existingUser = usersDB.find(u => u.username.toLowerCase() === username.toLowerCase());

            if (existingUser) {
                if (existingUser.password !== password) {
                    alert('Incorrect Password for student account!');
                    return;
                }
                state.currentUser = existingUser;
                alert(`Logged in to Student Account: ${existingUser.name} (${existingUser.xp} XP)`);
            } else {
                const newUser = {
                    username: username,
                    password: password,
                    name: username.charAt(0).toUpperCase() + username.slice(1),
                    role: 'STUDENT',
                    grade: grade,
                    school: school || 'Govt School, MP',
                    xp: 100,
                    streakDays: 1
                };
                usersDB.push(newUser);
                saveUsersToStorage(usersDB);
                state.currentUser = newUser;
                alert(`New Student Account Created! Welcome, ${newUser.name}.`);
            }

            localStorage.setItem('lq_current_user', JSON.stringify(state.currentUser));
            updateNavigationAndRoleUI();
            if (loginModal) loginModal.classList.add('hidden');
            switchTab('ai-solver');
        });
    }

    // Teacher Verified DB Auth Submit
    if (teacherForm) {
        teacherForm.addEventListener('submit', (e) => {
            e.preventDefault();
            const teacherId = document.getElementById('teacher-id-input').value.trim();
            const password = document.getElementById('teacher-pass-input').value.trim();

            const foundTeacher = state.teacherDatabase.find(t => t.teacherId.toUpperCase() === teacherId.toUpperCase() && t.password === password);

            if (foundTeacher) {
                state.currentUser = {
                    username: foundTeacher.teacherId,
                    name: foundTeacher.name,
                    role: 'TEACHER',
                    grade: 'All Grades',
                    teacherId: foundTeacher.teacherId,
                    school: foundTeacher.school,
                    xp: 0,
                    streakDays: 0
                };

                localStorage.setItem('lq_current_user', JSON.stringify(state.currentUser));
                updateNavigationAndRoleUI();
                if (loginModal) loginModal.classList.add('hidden');
                switchTab('teacher');
                alert(`Authentication Successful! Welcome, ${foundTeacher.name} (${foundTeacher.teacherId}).`);
            } else {
                alert('Authentication Failed! Invalid Teacher ID or Password. Valid IDs: MP-TEACHER-101, MP-TEACHER-102, MP-TEACHER-103.');
            }
        });
    }

    // Teacher Studio Subtabs
    const btnTInbox = document.getElementById('btn-t-inbox');
    const btnTAnalytics = document.getElementById('btn-t-analytics');
    const btnTGrading = document.getElementById('btn-t-grading');
    const btnTTeacherAI = document.getElementById('btn-t-teacher-ai');
    const btnTCreateMod = document.getElementById('btn-t-create-module');
    const btnTCreateAssign = document.getElementById('btn-t-create-assignment');
    const btnTManageAssign = document.getElementById('btn-t-manage-assignments');
    const btnTManageMod = document.getElementById('btn-t-manage-modules');

    const secInbox = document.getElementById('teacher-sec-inbox');
    const secAnalytics = document.getElementById('teacher-sec-analytics');
    const secGrading = document.getElementById('teacher-sec-grading');
    const secTeacherAI = document.getElementById('teacher-sec-teacher-ai');
    const secCreateMod = document.getElementById('teacher-sec-create-module');
    const secCreateAssign = document.getElementById('teacher-sec-create-assignment');
    const secManageAssign = document.getElementById('teacher-sec-manage-assignments');
    const secManageMod = document.getElementById('teacher-sec-manage-modules');

    function resetTeacherSubtabs() {
        [btnTInbox, btnTAnalytics, btnTGrading, btnTTeacherAI, btnTCreateMod, btnTCreateAssign, btnTManageAssign, btnTManageMod].forEach(b => b && b.classList.remove('active'));
        [secInbox, secAnalytics, secGrading, secTeacherAI, secCreateMod, secCreateAssign, secManageMod, secManageAssign].forEach(s => s && s.classList.add('hidden'));
    }

    if (btnTInbox) btnTInbox.addEventListener('click', () => { resetTeacherSubtabs(); btnTInbox.classList.add('active'); if (secInbox) secInbox.classList.remove('hidden'); renderTeacherPortal(); });
    if (btnTAnalytics) btnTAnalytics.addEventListener('click', () => { resetTeacherSubtabs(); btnTAnalytics.classList.add('active'); if (secAnalytics) secAnalytics.classList.remove('hidden'); renderTeacherQuizAnalytics(); });
    if (btnTGrading) btnTGrading.addEventListener('click', () => { resetTeacherSubtabs(); btnTGrading.classList.add('active'); if (secGrading) secGrading.classList.remove('hidden'); renderTeacherGrading(); });
    if (btnTTeacherAI) btnTTeacherAI.addEventListener('click', () => { resetTeacherSubtabs(); btnTTeacherAI.classList.add('active'); if (secTeacherAI) secTeacherAI.classList.remove('hidden'); });
    if (btnTCreateMod) btnTCreateMod.addEventListener('click', () => { resetTeacherSubtabs(); btnTCreateMod.classList.add('active'); if (secCreateMod) secCreateMod.classList.remove('hidden'); });
    if (btnTCreateAssign) btnTCreateAssign.addEventListener('click', () => { resetTeacherSubtabs(); btnTCreateAssign.classList.add('active'); if (secCreateAssign) secCreateAssign.classList.remove('hidden'); });
    if (btnTManageAssign) btnTManageAssign.addEventListener('click', () => { resetTeacherSubtabs(); btnTManageAssign.classList.add('active'); if (secManageAssign) secManageAssign.classList.remove('hidden'); renderTeacherManageAssignmentsList(); });
    if (btnTManageMod) btnTManageMod.addEventListener('click', () => { resetTeacherSubtabs(); btnTManageMod.classList.add('active'); if (secManageMod) secManageMod.classList.remove('hidden'); renderTeacherManageModulesList(); });

    // Inbox Filters & Search
    const inboxFilterAll = document.getElementById('inbox-filter-all-btn');
    const inboxFilterPending = document.getElementById('inbox-filter-pending-btn');
    const inboxFilterResolved = document.getElementById('inbox-filter-resolved-btn');
    const inboxSearchInput = document.getElementById('inbox-search-input');
    const inboxGradeFilter = document.getElementById('inbox-grade-filter');

    if (inboxFilterAll) inboxFilterAll.addEventListener('click', () => {
        state.inboxFilterStatus = 'ALL';
        [inboxFilterAll, inboxFilterPending, inboxFilterResolved].forEach(b => b && b.classList.remove('active'));
        inboxFilterAll.classList.add('active');
        renderTeacherPortal();
    });
    if (inboxFilterPending) inboxFilterPending.addEventListener('click', () => {
        state.inboxFilterStatus = 'PENDING';
        [inboxFilterAll, inboxFilterPending, inboxFilterResolved].forEach(b => b && b.classList.remove('active'));
        inboxFilterPending.classList.add('active');
        renderTeacherPortal();
    });
    if (inboxFilterResolved) inboxFilterResolved.addEventListener('click', () => {
        state.inboxFilterStatus = 'RESOLVED';
        [inboxFilterAll, inboxFilterPending, inboxFilterResolved].forEach(b => b && b.classList.remove('active'));
        inboxFilterResolved.classList.add('active');
        renderTeacherPortal();
    });
    if (inboxSearchInput) inboxSearchInput.addEventListener('input', (e) => {
        state.inboxSearchQuery = e.target.value.trim().toLowerCase();
        renderTeacherPortal();
    });
    if (inboxGradeFilter) inboxGradeFilter.addEventListener('change', (e) => {
        state.inboxGradeFilter = e.target.value;
        renderTeacherPortal();
    });

    // Analytics Filters
    const analyticsGradeFilter = document.getElementById('analytics-grade-filter');
    const analyticsSubjectFilter = document.getElementById('analytics-subject-filter');
    const rosterSearchInput = document.getElementById('roster-search-input');

    if (analyticsGradeFilter) analyticsGradeFilter.addEventListener('change', (e) => {
        state.analyticsGradeFilter = e.target.value;
        renderTeacherQuizAnalytics();
    });
    if (analyticsSubjectFilter) analyticsSubjectFilter.addEventListener('change', (e) => {
        state.analyticsSubjectFilter = e.target.value;
        renderTeacherQuizAnalytics();
    });
    if (rosterSearchInput) rosterSearchInput.addEventListener('input', (e) => {
        state.analyticsRosterSearch = e.target.value.trim().toLowerCase();
        renderTeacherQuizAnalytics();
    });

    // Grading Filters
    const gradingAssignFilter = document.getElementById('grading-assignment-filter');
    const gradingStatusFilter = document.getElementById('grading-status-filter');
    const gradingClassFilter = document.getElementById('grading-class-filter');
    const gradingRefreshBtn = document.getElementById('grading-refresh-btn');

    if (gradingAssignFilter) gradingAssignFilter.addEventListener('change', (e) => {
        state.gradingAssignmentFilter = e.target.value;
        renderTeacherGrading();
    });
    if (gradingStatusFilter) gradingStatusFilter.addEventListener('change', (e) => {
        state.gradingStatusFilter = e.target.value;
        renderTeacherGrading();
    });
    if (gradingClassFilter) gradingClassFilter.addEventListener('change', (e) => {
        state.gradingClassFilter = e.target.value;
        renderTeacherGrading();
    });
    if (gradingRefreshBtn) gradingRefreshBtn.addEventListener('click', () => {
        state.submissionsList = getStoredSubmissions();
        renderTeacherGrading();
        alert('Submissions refreshed!');
    });

    // Student Diagnostic Modal Close
    const closeDiagModal = document.getElementById('close-diagnostic-modal');
    const closeDiagBtn = document.getElementById('close-diag-btn');
    const diagModal = document.getElementById('student-diagnostic-modal');
    if (closeDiagModal && diagModal) closeDiagModal.addEventListener('click', () => diagModal.classList.add('hidden'));
    if (closeDiagBtn && diagModal) closeDiagBtn.addEventListener('click', () => diagModal.classList.add('hidden'));

    // Student Submit Assignment Modal Handlers
    const submitModal = document.getElementById('submit-assignment-modal');
    const closeSubmitModal = document.getElementById('close-submit-modal');
    const cancelSubmitBtn = document.getElementById('modal-submit-cancel-btn');
    const studentSubmitForm = document.getElementById('student-submit-form');

    if (closeSubmitModal && submitModal) closeSubmitModal.addEventListener('click', () => submitModal.classList.add('hidden'));
    if (cancelSubmitBtn && submitModal) cancelSubmitBtn.addEventListener('click', () => submitModal.classList.add('hidden'));

    if (studentSubmitForm) {
        studentSubmitForm.addEventListener('submit', (e) => {
            e.preventDefault();
            const assignId = document.getElementById('modal-submit-assign-id').value;
            const responseText = document.getElementById('modal-submit-response').value.trim();
            const attachmentNote = document.getElementById('modal-submit-attachment-note').value.trim();

            if (!responseText) {
                alert('Please enter your written response or solution!');
                return;
            }

            const allAssignments = getStoredAssignments();
            const targetAssignment = allAssignments.find(a => a.id === assignId);
            const assignTitle = targetAssignment ? targetAssignment.title : 'Class Assignment';

            const subs = getStoredSubmissions();
            // Check if already submitted by this student
            const existingSubIndex = subs.findIndex(s => s.assignmentId === assignId && s.studentId === (state.currentUser.username || 'student'));

            const newSubmission = {
                id: 'sub_' + Date.now(),
                assignmentId: assignId,
                assignmentTitle: assignTitle,
                studentId: state.currentUser.username || 'student',
                studentName: state.currentUser.name || 'Student',
                grade: state.currentUser.grade || 'Class 8',
                school: state.currentUser.school || 'Govt School, MP',
                submittedAt: Date.now(),
                content: responseText,
                attachmentNote: attachmentNote || 'Student Notebook Entry',
                status: 'PENDING',
                marks: null,
                maxMarks: 10,
                gradeLetter: '',
                teacherRemarks: '',
                gradedBy: '',
                gradedAt: null
            };

            if (existingSubIndex >= 0) {
                subs[existingSubIndex] = newSubmission;
            } else {
                subs.unshift(newSubmission);
            }

            saveSubmissionsToStorage(subs);
            state.submissionsList = subs;

            // Award XP
            state.currentUser.xp = (state.currentUser.xp || 500) + 50;
            localStorage.setItem('lq_current_user', JSON.stringify(state.currentUser));

            renderHeaderStats();
            renderStudentAssignments();
            renderTeacherGrading();

            if (submitModal) submitModal.classList.add('hidden');
            studentSubmitForm.reset();
            alert(`Assignment submitted successfully! +50 XP awarded. Awaiting teacher evaluation.`);
        });
    }


    // Teacher Custom Assignment Creation Submit
    const createAssignForm = document.getElementById('create-assignment-form');
    if (createAssignForm) {
        createAssignForm.addEventListener('submit', (e) => {
            e.preventDefault();
            const grade = document.getElementById('ta-grade').value;
            const subject = document.getElementById('ta-subject').value;
            const type = document.getElementById('ta-type').value;
            const title = document.getElementById('ta-title').value.trim();
            const duedate = document.getElementById('ta-duedate').value;
            const instructions = document.getElementById('ta-instructions').value.trim();

            const newAssign = {
                id: 'a_' + Date.now(),
                grade: grade,
                subject: subject,
                type: type,
                title: title,
                instructions: instructions,
                dueDate: duedate,
                timestamp: Date.now(),
                submitted: false
            };

            state.selectedAssignmentGrade = grade;
            saveAssignmentToStorage(newAssign);
            renderStudentAssignments();
            renderTeacherManageAssignmentsList();
            alert(`Assignment "${title}" (${type}) successfully published to ${grade} (${subject})!`);
            createAssignForm.reset();
        });
    }

    // TEACHER AI HELPER - GENERATE & PUBLISH (Powered by Gemini AI)
    const taiGenerateBtn = document.getElementById('tai-generate-btn');
    if (taiGenerateBtn) {
        taiGenerateBtn.addEventListener('click', async () => {
            const grade = document.getElementById('tai-grade').value;
            const subject = document.getElementById('tai-subject').value;
            const topicPrompt = document.getElementById('tai-prompt-input').value.trim() || 'प्रकाश का परावर्तन एवं अपवर्तन';

            // Show loading
            taiGenerateBtn.disabled = true;
            taiGenerateBtn.innerText = '⏳ Generating with Gemini AI...';
            const previewDiv = document.getElementById('tai-generated-preview');
            if (previewDiv) {
                previewDiv.classList.remove('hidden');
                document.getElementById('tai-preview-title').innerText = `⏳ Generating: ${topicPrompt}...`;
                document.getElementById('tai-preview-body').innerText = 'Gemini AI is creating comprehensive MP Board study notes. Please wait...';
            }

            const teacherName = state.currentUser?.name || 'Verified Teacher';
            const geminiPrompt = `You are an expert MP Board (MPBSE) curriculum teacher assistant helping ${teacherName} create detailed study notes for ${grade} ${subject} students.

Create comprehensive, bilingual (Hindi + English) study notes for the following topic:
Topic: ${topicPrompt}

Format as:
📘 MPBSE APPROVED CURRICULUM STUDY NOTES

**विषय परिचय (Introduction):**
[2-3 sentences introduction in Hindi]

**मुख्य अवधारणाएँ (Key Concepts):**
1. [Concept 1 - Hindi + English]
2. [Concept 2]
3. [Concept 3]

**महत्वपूर्ण सूत्र / नियम (Formulas / Rules):**
[List key formulas or rules]

**उदाहरण (Example):**
[A solved example relevant to MP Board curriculum]

**अभ्यास प्रश्न (Practice Questions):**
1. [Practice Q1]
2. [Practice Q2]

**सार (Summary):**
[One paragraph summary]`;

            const geminiText = await callGeminiAPI(geminiPrompt);
            const previewTitle = `${grade} ${subject}: ${topicPrompt} (Teacher AI Notes)`;
            let previewBody;

            if (geminiText) {
                previewBody = geminiText;
            } else {
                // Fallback to local engine
                const sol = generateUniversalAISolution(topicPrompt);
                previewBody = `📘 MPBSE APPROVED CURRICULUM STUDY NOTES (${sol.source}):

${sol.steps.map(s => `${s.num}:\n${s.text}`).join('\n\n')}

📌 सार संक्षेप:
${sol.shortSummary}`;
            }

            state.generatedTeacherAIModule = {
                id: 'm_custom_' + Date.now(),
                grade: grade,
                subject: subject,
                timestamp: Date.now(),
                titleHindi: previewTitle,
                titleEnglish: `${grade} ${subject}: ${topicPrompt}`,
                lessons: [
                    { title: `1. ${topicPrompt} - मुख्य बिंदु एवं नोट्स`, content: previewBody }
                ]
            };

            document.getElementById('tai-preview-title').innerText = previewTitle;
            document.getElementById('tai-preview-body').innerText = previewBody;
            if (previewDiv) previewDiv.classList.remove('hidden');
            taiGenerateBtn.disabled = false;
            taiGenerateBtn.innerText = '✨ Generate MP Board Study Notes with AI';
        });
    }

    const taiPublishBtn = document.getElementById('tai-publish-btn');
    if (taiPublishBtn) {
        taiPublishBtn.addEventListener('click', () => {
            if (state.generatedTeacherAIModule) {
                saveCustomModuleToStorage(state.generatedTeacherAIModule);
                state.selectedModuleGrade = state.generatedTeacherAIModule.grade;
                
                const gradeSelect = document.getElementById('module-grade-select');
                if (gradeSelect) gradeSelect.value = state.generatedTeacherAIModule.grade;

                renderLevels();
                alert(`AI Generated Module "${state.generatedTeacherAIModule.titleHindi}" successfully published for ${state.generatedTeacherAIModule.grade}!`);
                document.getElementById('tai-generated-preview').classList.add('hidden');
                document.getElementById('tai-prompt-input').value = '';
                state.generatedTeacherAIModule = null;
            }
        });
    }

    // MANUAL STUDY MODULE CREATION SUBMIT
    const createModForm = document.getElementById('create-module-form');
    if (createModForm) {
        createModForm.addEventListener('submit', (e) => {
            e.preventDefault();
            const grade = document.getElementById('tm-grade').value;
            const subject = document.getElementById('tm-subject').value;
            const titleHi = document.getElementById('tm-title-hi').value.trim();
            const titleEn = document.getElementById('tm-title-en').value.trim();
            const ch1Title = document.getElementById('tm-ch1-title').value.trim();
            const ch1Content = document.getElementById('tm-ch1-content').value.trim();

            if (!titleHi || !ch1Title || !ch1Content) {
                alert('Please fill in Module Title, Chapter Title, and Chapter Content!');
                return;
            }

            const newModule = {
                id: 'm_custom_' + Date.now(),
                grade: grade,
                subject: subject,
                timestamp: Date.now(),
                titleHindi: titleHi,
                titleEnglish: titleEn || titleHi,
                lessons: [{ title: ch1Title, content: ch1Content }]
            };

            saveCustomModuleToStorage(newModule);
            state.selectedModuleGrade = grade;

            const gradeSelect = document.getElementById('module-grade-select');
            if (gradeSelect) gradeSelect.value = grade;

            renderLevels();
            alert(`Custom Study Module "${titleHi}" successfully published for ${grade} (${subject})!`);
            createModForm.reset();
        });
    }

    // ADD MORE QUESTIONS BUTTON IN QUIZ BUILDER
    const addMoreQBtn = document.getElementById('add-more-q-btn');
    if (addMoreQBtn) {
        addMoreQBtn.addEventListener('click', () => {
            state.teacherQuestionCount++;
            const stack = document.getElementById('tq-questions-stack');
            const newQCard = document.createElement('div');
            newQCard.className = 'card tq-single-q-box';
            newQCard.style.cssText = 'background: #F8FAFC; border: 1px solid var(--border-color); padding: 16px; margin-bottom: 16px; position: relative;';
            newQCard.innerHTML = `
                <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 10px;">
                    <h4 style="color: var(--primary-saffron-dark);">Question ${state.teacherQuestionCount}</h4>
                    <button type="button" class="btn btn-secondary btn-sm delete-single-q-btn" style="color: var(--weak-red); border-color: var(--weak-red); font-size: 11px;">🗑️ Delete Question</button>
                </div>
                <div class="form-group">
                    <label>Question Text</label>
                    <input type="text" class="tq-question-input" placeholder="Type question text...">
                </div>
                <div class="grid grid-2">
                    <div class="form-group"><label>Option A (Index 0)</label><input type="text" class="tq-opt-0"></div>
                    <div class="form-group"><label>Option B (Index 1)</label><input type="text" class="tq-opt-1"></div>
                    <div class="form-group"><label>Option C (Index 2)</label><input type="text" class="tq-opt-2"></div>
                    <div class="form-group"><label>Option D (Index 3)</label><input type="text" class="tq-opt-3"></div>
                </div>
                <div class="form-group">
                    <label>Correct Option Index</label>
                    <select class="tq-correct-idx">
                        <option value="0">Option A (Index 0)</option>
                        <option value="1">Option B (Index 1)</option>
                        <option value="2">Option C (Index 2)</option>
                        <option value="3">Option D (Index 3)</option>
                    </select>
                </div>
                <div class="form-group">
                    <label>Explanation for Answer</label>
                    <input type="text" class="tq-explanation" placeholder="Type answer explanation...">
                </div>
            `;
            stack.appendChild(newQCard);

            newQCard.querySelector('.delete-single-q-btn').addEventListener('click', () => {
                newQCard.remove();
            });
        });
    }

    // FUNCTION TO EXECUTE QUIZ PUBLISHING
    function handlePublishQuizSubmit() {
        const grade = document.getElementById('tq-grade').value;
        const subject = document.getElementById('tq-subject').value;

        const qBoxes = document.querySelectorAll('.tq-single-q-box');
        if (!qBoxes || qBoxes.length === 0) {
            alert('Please add at least one question to publish!');
            return;
        }

        const questionsList = [];
        let isValid = true;

        qBoxes.forEach(box => {
            const qText = box.querySelector('.tq-question-input').value.trim();
            const opt0 = box.querySelector('.tq-opt-0').value.trim();
            const opt1 = box.querySelector('.tq-opt-1').value.trim();
            const opt2 = box.querySelector('.tq-opt-2').value.trim();
            const opt3 = box.querySelector('.tq-opt-3').value.trim();
            const correctIdx = parseInt(box.querySelector('.tq-correct-idx').value);
            const explanation = box.querySelector('.tq-explanation').value.trim();

            if (!qText || !opt0 || !opt1) {
                isValid = false;
            }

            questionsList.push({
                questionText: qText,
                options: [opt0, opt1, opt2 || 'Option C', opt3 || 'Option D'],
                correctAnswerIndex: correctIdx,
                explanation: explanation || 'Correct Answer.'
            });
        });

        if (!isValid) {
            alert('Please fill in all question text and option fields before publishing!');
            return;
        }

        saveCustomQuizSetToStorage({ grade, subject, questions: questionsList });

        // Update current active quiz
        initQuiz();

        alert(`Quiz (${questionsList.length} Questions) successfully published to ${grade} (${subject})!`);

        state.teacherQuestionCount = 1;
        document.getElementById('tq-questions-stack').innerHTML = `
            <div class="card tq-single-q-box" style="background: #F8FAFC; border: 1px solid var(--border-color); padding: 16px; margin-bottom: 16px; position: relative;">
                <h4 style="margin-bottom: 10px; color: var(--primary-saffron-dark);">Question 1</h4>
                <div class="form-group"><label>Question Text</label><input type="text" class="tq-question-input" placeholder="e.g. सोडियम धातु को किस द्रव में रखा जाता है?"></div>
                <div class="grid grid-2">
                    <div class="form-group"><label>Option A (Index 0)</label><input type="text" class="tq-opt-0" placeholder="e.g. केरोसिन तेल में (Kerosene)"></div>
                    <div class="form-group"><label>Option B (Index 1)</label><input type="text" class="tq-opt-1" placeholder="e.g. पानी में"></div>
                    <div class="form-group"><label>Option C (Index 2)</label><input type="text" class="tq-opt-2" placeholder="e.g. ऐल्कोहॉल में"></div>
                    <div class="form-group"><label>Option D (Index 3)</label><input type="text" class="tq-opt-3" placeholder="e.g. ईथर में"></div>
                </div>
                <div class="form-group"><label>Correct Option Index</label><select class="tq-correct-idx"><option value="0">Option A (Index 0)</option><option value="1">Option B (Index 1)</option><option value="2">Option C (Index 2)</option><option value="3">Option D (Index 3)</option></select></div>
                <div class="form-group"><label>Explanation for Answer</label><input type="text" class="tq-explanation" placeholder="e.g. सोडियम वायु व जल से तीव्र अभिक्रिया करता है।"></div>
            </div>
        `;
    }

    // PUBLISH QUIZ BUTTON BINDING
    const publishQuizBtn = document.getElementById('publish-quiz-btn');
    if (publishQuizBtn) {
        publishQuizBtn.addEventListener('click', (e) => {
            e.preventDefault();
            handlePublishQuizSubmit();
        });
    }

    // AI Solver events
    const aiSolveBtn = document.getElementById('ai-solve-btn');
    if (aiSolveBtn) aiSolveBtn.addEventListener('click', handleLiveAISolve);

    const aiVoiceBtn = document.getElementById('ai-voice-btn');
    if (aiVoiceBtn) aiVoiceBtn.addEventListener('click', () => handleSpeechInput('ai-question-input'));

    document.querySelectorAll('.sample-btn').forEach(btn => {
        btn.addEventListener('click', (e) => {
            const q = e.target.dataset.q || e.target.innerText;
            document.getElementById('ai-question-input').value = q;
            handleLiveAISolve();
        });
    });

    const aiTtsBtn = document.getElementById('ai-tts-btn');
    if (aiTtsBtn) {
        aiTtsBtn.addEventListener('click', () => {
            const text = document.getElementById('solution-steps').innerText;
            speakTextHindi(text || "यह एआई समाधान है।");
        });
    }

    // AI Escalation Button
    const aiEscalateBtn = document.getElementById('ai-escalate-btn');
    if (aiEscalateBtn) {
        aiEscalateBtn.addEventListener('click', handleEscalateAISolutionToTeacher);
    }

    // MP Board Assignment Filters
    const assignGradeSelect = document.getElementById('assign-grade-select');
    if (assignGradeSelect) {
        assignGradeSelect.addEventListener('change', (e) => {
            state.selectedAssignmentGrade = e.target.value;
            renderStudentAssignments();
        });
    }

    const assignSubjectSelect = document.getElementById('assign-subject-select');
    if (assignSubjectSelect) {
        assignSubjectSelect.addEventListener('change', (e) => {
            state.selectedAssignmentSubject = e.target.value;
            renderStudentAssignments();
        });
    }

    // MP Board Curriculum Filters
    const gradeSelect = document.getElementById('module-grade-select');
    if (gradeSelect) {
        gradeSelect.addEventListener('change', (e) => {
            state.selectedModuleGrade = e.target.value;
            renderLevels();
        });
    }

    const subjectSelect = document.getElementById('module-subject-select');
    if (subjectSelect) {
        subjectSelect.addEventListener('change', (e) => {
            state.selectedModuleSubject = e.target.value;
            renderLevels();
        });
    }

    const quizSubjectSelect = document.getElementById('quiz-subject-select');
    if (quizSubjectSelect) {
        quizSubjectSelect.addEventListener('change', (e) => {
            state.selectedQuizSubject = e.target.value;
            initQuiz();
        });
    }

    // Opportunities Subtabs
    const btnScholarships = document.getElementById('btn-show-scholarships');
    const btnCareers = document.getElementById('btn-show-careers');
    if (btnScholarships && btnCareers) {
        btnScholarships.addEventListener('click', () => {
            btnScholarships.classList.add('active');
            btnCareers.classList.remove('active');
            document.getElementById('section-scholarships').classList.remove('hidden');
            document.getElementById('section-careers').classList.add('hidden');
        });

        btnCareers.addEventListener('click', () => {
            btnCareers.classList.add('active');
            btnScholarships.classList.remove('active');
            document.getElementById('section-careers').classList.remove('hidden');
            document.getElementById('section-scholarships').classList.add('hidden');
        });
    }

    const retryQuizBtn = document.getElementById('retry-quiz-btn');
    if (retryQuizBtn) retryQuizBtn.addEventListener('click', () => initQuiz());

    const newPuzzleBtn = document.getElementById('generate-new-puzzle-btn');
    if (newPuzzleBtn) {
        newPuzzleBtn.addEventListener('click', () => {
            renderPuzzle();
        });
    }
}

/* RENDER ASSIGNMENT DELETION MANAGER & TEACHER GRADING AREA */
function renderTeacherManageAssignmentsList() {
    const container = document.getElementById('teacher-manage-assignments-list');
    if (!container) return;
    container.innerHTML = '';

    const allAssignments = getStoredAssignments();

    if (allAssignments.length === 0) {
        container.innerHTML = '<p style="color: var(--text-secondary); text-align: center; padding: 20px;">No published assignments found.</p>';
        return;
    }

    allAssignments.forEach(a => {
        const card = document.createElement('div');
        card.className = 'card';
        card.style.cssText = 'background: #F8FAFC; margin-bottom: 16px; padding: 16px;';

        card.innerHTML = `
            <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 10px; flex-wrap: wrap; gap: 8px;">
                <div>
                    <strong style="font-size: 16px;">${a.title}</strong>
                    <small style="display: block; color: var(--text-secondary);">${a.grade} • ${a.subject} (${a.type}) • Due: ${a.dueDate}</small>
                </div>
                <button class="btn btn-secondary btn-sm delete-assign-manager-btn" data-id="${a.id}" style="color: var(--weak-red); border-color: var(--weak-red); font-weight: 700;">
                    🗑️ Delete Assignment
                </button>
            </div>

            <!-- Student Submissions & Grading Section -->
            <div style="margin-top: 12px; background: white; padding: 14px; border-radius: 8px; border: 1px solid var(--border-color);">
                <h4 style="font-size: 14px; color: var(--primary-saffron-dark); margin-bottom: 8px;">📝 Student Submissions & Grading Area:</h4>
                ${a.submitted ? `
                    <div style="font-size: 13px; margin-bottom: 8px;">
                        <strong>Submitted Answer / Report Notes:</strong>
                        <div style="background: var(--bg-surface); padding: 8px; border-radius: 4px; margin-top: 4px; font-style: italic;">"${a.studentResponse || 'Completed'}"</div>
                    </div>
                    
                    <div style="display: flex; gap: 10px; align-items: center; flex-wrap: wrap; margin-top: 10px;">
                        <input type="text" class="teacher-grade-input" id="grade-val-${a.id}" placeholder="Enter Grade (e.g. A+ / 95%)" value="${a.teacherGrade || ''}" style="padding: 6px; border: 1px solid var(--border-color); border-radius: 4px; font-size: 12px; width: 160px;">
                        <input type="text" class="teacher-feedback-input" id="feedback-val-${a.id}" placeholder="Teacher Feedback / Remarks" value="${a.teacherFeedback || ''}" style="padding: 6px; border: 1px solid var(--border-color); border-radius: 4px; font-size: 12px; flex: 1; min-width: 200px;">
                        <button class="btn btn-primary btn-sm save-grade-btn" data-id="${a.id}">💾 Save Grade & Feedback</button>
                    </div>
                ` : `
                    <p style="font-size: 12px; color: var(--text-secondary);">No student submissions received yet for this assignment.</p>
                `}
            </div>
        `;
        container.appendChild(card);
    });

    document.querySelectorAll('.delete-assign-manager-btn').forEach(btn => {
        btn.addEventListener('click', (e) => {
            const id = e.target.dataset.id;
            if (confirm('Are you sure you want to remove this assignment from the platform?')) {
                deleteAssignmentFromStorage(id);
                renderTeacherManageAssignmentsList();
                renderStudentAssignments();
                alert('Assignment removed successfully!');
            }
        });
    });

    document.querySelectorAll('.save-grade-btn').forEach(btn => {
        btn.addEventListener('click', (e) => {
            const id = e.target.dataset.id;
            const gradeVal = document.getElementById(`grade-val-${id}`).value.trim();
            const feedbackVal = document.getElementById(`feedback-val-${id}`).value.trim();

            const all = getStoredAssignments();
            const target = all.find(a => a.id === id);
            if (target) {
                target.teacherGrade = gradeVal || 'Graded (Satisfactory)';
                target.teacherFeedback = feedbackVal || 'Good effort!';
                localStorage.setItem('lq_assignments_db', JSON.stringify(all));
                renderTeacherManageAssignmentsList();
                renderStudentAssignments();
                alert('Grade & Teacher Feedback saved successfully!');
            }
        });
    });
}
/* RENDER MODULE DELETION MANAGER IN TEACHER PORTAL */
function renderTeacherManageModulesList() {
    const container = document.getElementById('teacher-manage-modules-list');
    if (!container) return;
    container.innerHTML = '';

    const customModules = getStoredModules();
    const allCombined = [...state.mpBoardModules, ...customModules];

    allCombined.forEach(m => {
        const isCustom = m.id.startsWith('m_custom_');
        const card = document.createElement('div');
        card.className = 'card';
        card.style.cssText = 'background: #F8FAFC; margin-bottom: 12px; display: flex; justify-content: space-between; align-items: center; flex-wrap: wrap; gap: 10px;';

        card.innerHTML = `
            <div>
                <strong style="font-size: 15px;">${m.titleHindi}</strong>
                <small style="display: block; color: var(--text-secondary);">${m.grade} • ${m.subject} ${isCustom ? '(Teacher Custom)' : '(Default Syllabus)'}</small>
            </div>
            <button class="btn btn-secondary btn-sm delete-mod-manager-btn" data-id="${m.id}" style="color: var(--weak-red); border-color: var(--weak-red); font-weight: 700;">
                🗑️ Delete Module
            </button>
        `;
        container.appendChild(card);
    });

    document.querySelectorAll('.delete-mod-manager-btn').forEach(btn => {
        btn.addEventListener('click', (e) => {
            const id = e.target.dataset.id;
            if (confirm('Are you sure you want to remove this module from the platform?')) {
                if (id.startsWith('m_custom_')) {
                    deleteCustomModuleFromStorage(id);
                } else {
                    state.mpBoardModules = state.mpBoardModules.filter(m => m.id !== id);
                }
                renderTeacherManageModulesList();
                renderLevels();
                alert('Module removed successfully!');
            }
        });
    });
}

/* RENDER DETAILED STUDENT PERFORMANCE ANALYTICS IN TEACHER PORTAL */
function renderTeacherQuizAnalytics() {
    // 1. Executive Metrics
    const roster = getEnrolledStudentsRoster();
    const targetGrade = state.analyticsGradeFilter || 'Class 8';
    const targetSubject = state.analyticsSubjectFilter || 'All';

    let filteredRoster = roster;
    if (targetGrade !== 'All') {
        filteredRoster = filteredRoster.filter(s => s.grade === targetGrade);
    }
    if (state.analyticsRosterSearch) {
        const q = state.analyticsRosterSearch;
        filteredRoster = filteredRoster.filter(s => 
            s.name.toLowerCase().includes(q) || 
            s.school.toLowerCase().includes(q) || 
            s.username.toLowerCase().includes(q)
        );
    }

    // KPI computations
    const totalQuizzes = roster.reduce((acc, s) => acc + (s.quizzesTaken || 0), 0);
    const avgScore = roster.length > 0 ? Math.round(roster.reduce((acc, s) => acc + (s.avgScore || 0), 0) / roster.length * 10) / 10 : 78.4;
    const atRiskCount = roster.filter(s => (s.avgScore || 0) < 60).length;

    const classAvgEl = document.getElementById('analytics-class-avg-score');
    if (classAvgEl) classAvgEl.innerText = `${avgScore}%`;

    const totalQuizzesEl = document.getElementById('analytics-total-quizzes');
    if (totalQuizzesEl) totalQuizzesEl.innerText = totalQuizzes;

    const atRiskEl = document.getElementById('analytics-at-risk-count');
    if (atRiskEl) atRiskEl.innerText = atRiskCount;

    // Submission & Grading Rate
    const allSubsForRate = getStoredSubmissions();
    const rateEl = document.getElementById('analytics-assignments-graded-rate');
    if (rateEl) {
        const totalExpected = roster.length * 2; // 2 assignments per student
        const submitted = allSubsForRate.length;
        const submissionRate = totalExpected > 0 ? Math.min(100, Math.round((submitted / totalExpected) * 100)) : 88;
        rateEl.innerText = `${submissionRate}%`;
    }

    // 2. Populate Roster Table
    const rosterContainer = document.getElementById('teacher-analytics-roster-container');
    if (rosterContainer) {
        rosterContainer.innerHTML = '';
        if (filteredRoster.length === 0) {
            rosterContainer.innerHTML = `<tr><td colspan="8" style="text-align: center; padding: 20px; color: var(--text-secondary);">No students found matching current filter.</td></tr>`;
        } else {
            filteredRoster.forEach(s => {
                const tr = document.createElement('tr');
                let masteryBadgeClass = 'mastery-badge mastered';
                let masteryText = 'Mastered (≥85%)';
                if ((s.avgScore || 0) < 60) {
                    masteryBadgeClass = 'mastery-badge weak';
                    masteryText = 'Needs Help (<60%)';
                } else if ((s.avgScore || 0) < 85) {
                    masteryBadgeClass = 'mastery-badge developing';
                    masteryText = 'Developing (60-84%)';
                }

                tr.innerHTML = `
                    <td style="padding: 10px 14px; font-weight: 700; color: var(--text-primary);">
                        🧑‍🎓 ${s.name}
                        <div style="font-size: 11px; color: var(--text-secondary); font-weight: normal;">ID: ${s.username}</div>
                    </td>
                    <td style="padding: 10px 14px;"><span class="subject-badge" style="font-size: 11px;">${s.grade}</span></td>
                    <td style="padding: 10px 14px; color: var(--text-secondary);">${s.school}</td>
                    <td style="padding: 10px 14px; font-weight: 600;">${s.quizzesTaken || 10}</td>
                    <td style="padding: 10px 14px;">
                        <strong style="color: ${s.avgScore >= 85 ? 'var(--forest-green)' : s.avgScore >= 60 ? 'var(--developing-orange)' : 'var(--weak-red)'}; font-size: 14px;">
                            ${s.avgScore || 75}%
                        </strong>
                    </td>
                    <td style="padding: 10px 14px; color: var(--text-secondary);">${s.assignmentsDone || 3} submitted</td>
                    <td style="padding: 10px 14px;"><span class="${masteryBadgeClass}">${masteryText}</span></td>
                    <td style="padding: 10px 14px; text-align: center;">
                        <button class="btn btn-secondary btn-sm view-diag-btn" data-username="${s.username}" style="font-size: 11px; padding: 4px 8px;">🔍 Diagnostic</button>
                    </td>
                `;
                rosterContainer.appendChild(tr);
            });
        }
    }

    // Wire Diagnostic Buttons
    document.querySelectorAll('.view-diag-btn').forEach(btn => {
        btn.addEventListener('click', (e) => {
            const username = e.target.closest('button').dataset.username;
            openStudentDiagnosticModal(username);
        });
    });

    // 3. Subject Mastery Bars (Dynamic from student roster)
    const subjectBarsContainer = document.getElementById('analytics-subject-bars');
    if (subjectBarsContainer) {
        subjectBarsContainer.innerHTML = '';
        const subjectKeys = ['Science', 'Mathematics', 'SocialScience', 'Hindi', 'English'];
        const subjectLabels = {
            'Science': 'Science (विज्ञान)',
            'Mathematics': 'Mathematics (गणित)',
            'SocialScience': 'Social Science (सामाजिक विज्ञान)',
            'Hindi': 'Hindi (हिन्दी)',
            'English': 'English (अंग्रेज़ी)'
        };
        const rosterForBars = filteredRoster.length > 0 ? filteredRoster : roster;
        const subjectStats = subjectKeys.map(key => {
            const studentsWithData = rosterForBars.filter(s => s.strengths && s.strengths[key] !== undefined);
            const pct = studentsWithData.length > 0
                ? Math.round(studentsWithData.reduce((acc, s) => acc + (s.strengths[key] || 0), 0) / studentsWithData.length)
                : 75;
            return {
                subject: subjectLabels[key],
                pct,
                color: pct >= 85 ? 'var(--forest-green)' : pct >= 70 ? 'var(--primary-saffron)' : 'var(--developing-orange)'
            };
        }).sort((a, b) => b.pct - a.pct);

        subjectStats.forEach(sub => {
            const row = document.createElement('div');
            row.className = 'analytics-bar-container';
            row.innerHTML = `
                <div class="analytics-bar-header">
                    <span>${sub.subject}</span>
                    <span style="color: ${sub.color}; font-weight: 700;">${sub.pct}%</span>
                </div>
                <div class="analytics-bar-bg">
                    <div class="analytics-bar-fill" style="width: ${sub.pct}%; background: ${sub.color};"></div>
                </div>
            `;
            subjectBarsContainer.appendChild(row);
        });
    }

    // 4. Learning Gaps & Remediation Alerts
    const weakTopicsContainer = document.getElementById('analytics-weak-topics-list');
    if (weakTopicsContainer) {
        weakTopicsContainer.innerHTML = `
            <div class="weak-topic-item" style="border-left: 3px solid var(--weak-red);">
                <div>
                    <strong style="color: var(--weak-red); font-size: 13.5px; display: block;">एक चर वाले रैखिक समीकरण (Linear Equations)</strong>
                    <span style="font-size: 12px; color: var(--text-secondary);">Class 8 Mathematics • 46% Error Rate in transposing terms</span>
                </div>
                <button class="btn btn-primary btn-sm auto-remedy-btn" data-topic="एक चर वाले रैखिक समीकरण हल करने की विधियाँ" data-grade="Class 8" data-subject="Mathematics" style="font-size: 11px;">🤖 Create AI Notes</button>
            </div>
            <div class="weak-topic-item" style="border-left: 3px solid var(--developing-orange);">
                <div>
                    <strong style="color: var(--developing-orange); font-size: 13.5px; display: block;">प्रकाश परावर्तन एवं आपतन कोण (Angle of Reflection)</strong>
                    <span style="font-size: 12px; color: var(--text-secondary);">Class 8 Science • 38% Error Rate in ray diagrams</span>
                </div>
                <button class="btn btn-primary btn-sm auto-remedy-btn" data-topic="प्रकाश परावर्तन के नियम एवं किरण आरेख" data-grade="Class 8" data-subject="Science" style="font-size: 11px;">🤖 Create AI Notes</button>
            </div>
            <div class="weak-topic-item" style="border-left: 3px solid var(--streak-blue);">
                <div>
                    <strong style="color: var(--streak-blue); font-size: 13.5px; display: block;">पादपों में पोषण एवं प्रकाश संश्लेषण रासायनिक समीकरण</strong>
                    <span style="font-size: 12px; color: var(--text-secondary);">Class 7 Science • 32% Error Rate in CO2 assimilation</span>
                </div>
                <button class="btn btn-primary btn-sm auto-remedy-btn" data-topic="पादपों में प्रकाश संश्लेषण क्रिया एवं समीकरण" data-grade="Class 7" data-subject="Science" style="font-size: 11px;">🤖 Create AI Notes</button>
            </div>
        `;

        document.querySelectorAll('.auto-remedy-btn').forEach(btn => {
            btn.addEventListener('click', (e) => {
                const topic = e.target.dataset.topic;
                const grade = e.target.dataset.grade;
                const subject = e.target.dataset.subject;

                const btnAI = document.getElementById('btn-t-teacher-ai');
                if (btnAI) btnAI.click();

                const promptInput = document.getElementById('tai-prompt-input');
                const gradeSel = document.getElementById('tai-grade');
                const subSel = document.getElementById('tai-subject');

                if (promptInput) promptInput.value = topic;
                if (gradeSel) gradeSel.value = grade;
                if (subSel) subSel.value = subject;

                const genBtn = document.getElementById('tai-generate-btn');
                if (genBtn) genBtn.click();
            });
        });
    }

    // 5. Question Responses & Option Breakdown
    const breakdownContainer = document.getElementById('teacher-analytics-breakdown-container');
    if (breakdownContainer) {
        breakdownContainer.innerHTML = '';
        const quizAnalytics = getStoredQuizAnalytics();
        const filteredQA = quizAnalytics.filter(qa => {
            const matchGrade = (targetGrade === 'All' || qa.grade === targetGrade);
            const matchSub = (targetSubject === 'All' || qa.subject === targetSubject);
            return matchGrade && matchSub;
        });

        if (filteredQA.length === 0) {
            breakdownContainer.innerHTML = `<p style="padding: 16px; color: var(--text-secondary); text-align: center;">No quiz breakdown data for ${targetGrade} (${targetSubject}). Try selecting "All Subjects" or "Class 8".</p>`;
        } else {
            filteredQA.forEach(item => {
                const card = document.createElement('div');
                card.className = 'card';
                card.style.marginBottom = '16px';
                card.style.background = '#F8FAFC';

                let breakdownHTML = '';
                for (let [optLabel, optData] of Object.entries(item.breakdown)) {
                    const pctVal = parseInt(optData.pct || '0');
                    const isCorrect = optData.isCorrect;
                    breakdownHTML += `
                        <div style="margin-bottom: 8px;">
                            <div style="display: flex; justify-content: space-between; font-size: 13px; margin-bottom: 3px;">
                                <span>${optLabel} ${isCorrect ? '<strong style="color: var(--forest-green);">(✓ Correct Answer)</strong>' : ''}</span>
                                <strong style="color: ${isCorrect ? 'var(--forest-green)' : 'var(--text-primary)'};">${optData.pct} (${optData.count} Students)</strong>
                            </div>
                            <div style="width: 100%; height: 8px; background: #E2E8F0; border-radius: 4px; overflow: hidden;">
                                <div style="height: 100%; width: ${pctVal}%; background: ${isCorrect ? 'var(--forest-green)' : '#94A3B8'}; border-radius: 4px; transition: width 0.4s ease;"></div>
                            </div>
                        </div>
                    `;
                }

                card.innerHTML = `
                    <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 8px; flex-wrap: wrap; gap: 6px;">
                        <span class="subject-badge">${item.grade} • ${item.subject}</span>
                        <span style="font-size: 12px; color: var(--text-secondary); font-weight: 700;">Total Responses: ${item.totalResponses || 28} Students</span>
                    </div>
                    <h4 style="font-size: 15px; margin-bottom: 12px; color: var(--text-primary);">Q: ${item.questionText}</h4>
                    <div>${breakdownHTML}</div>
                `;
                breakdownContainer.appendChild(card);
            });
        }
    }
}

function openStudentDiagnosticModal(username) {
    const roster = getEnrolledStudentsRoster();
    const student = roster.find(s => s.username === username);
    if (!student) return;

    const modal = document.getElementById('student-diagnostic-modal');
    if (!modal) return;

    document.getElementById('diag-student-name').innerText = student.name;
    document.getElementById('diag-student-meta').innerText = `${student.grade} • ${student.school} (ID: ${student.username})`;
    document.getElementById('diag-overall-score').innerText = `${student.avgScore || 75}%`;
    document.getElementById('diag-quizzes-count').innerText = student.quizzesTaken || 12;
    document.getElementById('diag-assignments-count').innerText = student.assignmentsDone || 3;

    const barsContainer = document.getElementById('diag-subject-bars');
    if (barsContainer) {
        barsContainer.innerHTML = '';
        const strengths = student.strengths || { Science: 80, Mathematics: 70, SocialScience: 82, Hindi: 85, English: 74 };
        for (let [sub, score] of Object.entries(strengths)) {
            const color = score >= 85 ? 'var(--forest-green)' : score >= 60 ? 'var(--primary-saffron)' : 'var(--weak-red)';
            const div = document.createElement('div');
            div.className = 'analytics-bar-container';
            div.innerHTML = `
                <div class="analytics-bar-header">
                    <span>${sub}</span>
                    <span style="color: ${color}; font-weight: 700;">${score}%</span>
                </div>
                <div class="analytics-bar-bg">
                    <div class="analytics-bar-fill" style="width: ${score}%; background: ${color};"></div>
                </div>
            `;
            barsContainer.appendChild(div);
        }
    }

    const recEl = document.getElementById('diag-recommendation');
    if (recEl) {
        if ((student.avgScore || 0) >= 85) {
            recEl.innerText = `${student.name} demonstrates exceptional mastery across all subjects. Recommend advanced peer mentoring and entry into the MP State Science Talent Search Olympiad.`;
        } else if ((student.avgScore || 0) >= 60) {
            recEl.innerText = `${student.name} is progressing well. Additional practice on multi-step mathematical problems and science diagram analysis will elevate them to the Mastered bracket.`;
        } else {
            recEl.innerText = `Intervention required: ${student.name} is struggling with foundational equations and concept retention. Recommend assigned remedial module with step-by-step AI voice notes.`;
        }
    }

    modal.classList.remove('hidden');
}

/* TEACHER GRADING SYSTEM FOR ASSIGNMENTS */
function renderTeacherGrading() {
    const container = document.getElementById('teacher-grading-submissions-list');
    if (!container) return;
    container.innerHTML = '';

    const allSubs = getStoredSubmissions();
    const allAssignments = getStoredAssignments();

    // Populate assignment filter dropdown
    const assignFilter = document.getElementById('grading-assignment-filter');
    if (assignFilter && assignFilter.options.length <= 1) {
        allAssignments.forEach(a => {
            const opt = document.createElement('option');
            opt.value = a.id;
            opt.innerText = `${a.grade}: ${a.title.substring(0, 32)}...`;
            assignFilter.appendChild(opt);
        });
    }

    // KPI stats
    const pendingSubs = allSubs.filter(s => s.status === 'PENDING');
    const gradedSubs = allSubs.filter(s => s.status === 'GRADED');

    const totalEl = document.getElementById('grading-stat-total');
    if (totalEl) totalEl.innerText = allSubs.length;

    const pendingEl = document.getElementById('grading-stat-pending');
    if (pendingEl) pendingEl.innerText = pendingSubs.length;

    const gradedEl = document.getElementById('grading-stat-graded');
    if (gradedEl) gradedEl.innerText = gradedSubs.length;

    const avgEl = document.getElementById('grading-stat-avg-score');
    if (avgEl && gradedSubs.length > 0) {
        const sum = gradedSubs.reduce((acc, s) => acc + (s.marks || 0), 0);
        const avg = Math.round((sum / gradedSubs.length) * 10) / 10;
        avgEl.innerText = `${avg} / 10`;
    }

    // Apply filters
    let filteredSubs = allSubs;
    if (state.gradingAssignmentFilter && state.gradingAssignmentFilter !== 'All') {
        filteredSubs = filteredSubs.filter(s => s.assignmentId === state.gradingAssignmentFilter);
    }
    if (state.gradingStatusFilter && state.gradingStatusFilter !== 'All') {
        filteredSubs = filteredSubs.filter(s => s.status === state.gradingStatusFilter);
    }
    if (state.gradingClassFilter && state.gradingClassFilter !== 'All') {
        filteredSubs = filteredSubs.filter(s => (s.grade || 'Class 8') === state.gradingClassFilter);
    }

    if (filteredSubs.length === 0) {
        container.innerHTML = `
            <div style="text-align: center; padding: 30px; background: #F8FAFC; border-radius: var(--radius-md); border: 1px dashed var(--border-color); color: var(--text-secondary);">
                <span style="font-size: 28px; display: block; margin-bottom: 8px;">📑</span>
                <strong>No student submissions found matching the selected filter.</strong>
                <p style="font-size: 13px; margin-top: 4px;">Select "All Statuses" or choose another assignment above to evaluate student reports.</p>
            </div>
        `;
        return;
    }

    filteredSubs.forEach(sub => {
        const card = document.createElement('div');
        card.className = 'grading-submission-card';

        const assignObj = allAssignments.find(a => a.id === sub.assignmentId) || {};
        const assignType = assignObj.type || 'Homework Practice';
        const isGraded = (sub.status === 'GRADED');

        card.innerHTML = `
            <div style="display: flex; justify-content: space-between; align-items: flex-start; margin-bottom: 10px; flex-wrap: wrap; gap: 8px;">
                <div>
                    <span class="subject-badge">${sub.grade} • ${assignObj.subject || 'All Subjects'}</span>
                    <span class="mastery-badge ${assignType === 'Hands-on Project' ? 'developing' : 'mastered'}">${assignType}</span>
                    <h3 style="font-size: 16px; margin-top: 6px; color: var(--text-primary);">${sub.assignmentTitle}</h3>
                </div>
                <div>
                    ${isGraded ? `
                        <span class="badge-status-graded">✓ Graded: ${sub.gradeLetter} (${sub.marks}/${sub.maxMarks || 10})</span>
                    ` : `
                        <span class="badge-status-pending">⏳ Needs Teacher Evaluation</span>
                    `}
                </div>
            </div>

            <!-- Student Metadata -->
            <div style="display: flex; align-items: center; gap: 10px; padding: 8px 12px; background: #F1F5F9; border-radius: 6px; margin-bottom: 12px; font-size: 13px; flex-wrap: wrap;">
                <span>🧑‍🎓 <strong>${sub.studentName}</strong> (ID: ${sub.studentId})</span>
                <span>🏫 ${sub.school}</span>
                <span style="color: var(--text-secondary); margin-left: auto;">🕒 Submitted: ${formatTimestamp(sub.submittedAt)}</span>
            </div>

            <!-- Student Written Response Panel -->
            <div class="student-response-panel">
                <div style="font-size: 11px; font-weight: 700; color: var(--text-secondary); margin-bottom: 4px;">
                    📝 STUDENT SUBMITTED WORK / OBSERVATIONS:
                </div>
                <div style="white-space: pre-wrap; color: var(--text-primary); font-weight: 500;">${sub.content}</div>
                ${sub.attachmentNote ? `
                    <div style="font-size: 12px; color: var(--streak-blue); margin-top: 6px; font-weight: 600;">
                        📎 Verified Model Note: ${sub.attachmentNote}
                    </div>
                ` : ''}
            </div>

            <!-- Graded Banner (if already graded) -->
            ${isGraded ? `
                <div style="background: var(--forest-green-light); border: 1px solid var(--forest-green); border-radius: 8px; padding: 14px; margin-top: 10px;">
                    <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 6px;">
                        <span style="font-size: 15px; font-weight: 800; color: var(--forest-green);">
                            🏆 Awarded: Grade ${sub.gradeLetter} (${sub.marks} / ${sub.maxMarks || 10} Marks)
                        </span>
                        <button class="btn btn-secondary btn-sm toggle-edit-grade-btn" data-id="${sub.id}" style="font-size: 11px; padding: 4px 8px;">✏️ Edit Grade</button>
                    </div>
                    <div style="font-size: 13px; color: var(--text-primary); margin-bottom: 4px;">
                        👨‍🏫 <strong>Teacher Feedback:</strong> "${sub.teacherRemarks}"
                    </div>
                    <div style="font-size: 11px; color: var(--forest-green); font-weight: 600;">
                        Graded by ${sub.gradedBy || 'Verified Teacher'} • ${formatTimestamp(sub.gradedAt)}
                    </div>
                </div>
            ` : ''}

            <!-- Grading Evaluation Form Box (Visible if pending, or hidden toggle if already graded) -->
            <div class="grading-evaluation-box ${isGraded ? 'hidden' : ''}" id="eval-box-${sub.id}">
                <div style="font-weight: 700; font-size: 14px; color: var(--primary-saffron-dark); margin-bottom: 10px;">
                    🎯 Teacher Grading &amp; Feedback Desk
                </div>

                <div class="grid grid-2" style="margin-bottom: 12px;">
                    <div class="form-group" style="margin: 0;">
                        <label>Award Marks (अंक)</label>
                        <div style="display: flex; align-items: center; gap: 8px;">
                            <input type="number" id="marks-input-${sub.id}" min="0" max="${sub.maxMarks || 10}" value="${sub.marks !== null ? sub.marks : 8}" style="width: 80px; padding: 6px 10px; font-weight: 700; font-size: 15px; text-align: center; border: 1px solid var(--border-color); border-radius: 6px;">
                            <span style="font-size: 14px; font-weight: 600; color: var(--text-secondary);">/ ${sub.maxMarks || 10} Marks</span>
                        </div>
                    </div>

                    <div class="form-group" style="margin: 0;">
                        <label>Performance Letter Grade (ग्रेड)</label>
                        <select id="grade-letter-input-${sub.id}" style="padding: 6px 10px; font-size: 14px; font-weight: 700;">
                            <option value="A+" ${sub.gradeLetter === 'A+' ? 'selected' : ''}>Grade A+ (Outstanding / 95-100%)</option>
                            <option value="A" ${(!sub.gradeLetter || sub.gradeLetter === 'A') ? 'selected' : ''}>Grade A (Excellent / 85-94%)</option>
                            <option value="B+" ${sub.gradeLetter === 'B+' ? 'selected' : ''}>Grade B+ (Very Good / 75-84%)</option>
                            <option value="B" ${sub.gradeLetter === 'B' ? 'selected' : ''}>Grade B (Good / 60-74%)</option>
                            <option value="C" ${sub.gradeLetter === 'C' ? 'selected' : ''}>Grade C (Satisfactory / 45-59%)</option>
                            <option value="Needs Revision" ${sub.gradeLetter === 'Needs Revision' ? 'selected' : ''}>Needs Revision (पुनः कार्य करें)</option>
                        </select>
                    </div>
                </div>

                <div class="form-group" style="margin-bottom: 8px;">
                    <label>Teacher Feedback &amp; Constructive Remarks (शिक्षक टिप्पणी)</label>
                    <div style="margin-bottom: 6px;">
                        <span class="quick-feedback-chip grading-chip" data-id="${sub.id}" data-text="शाबाश! उत्कृष्ट प्रयोगात्मक विश्लेषण और सही निष्कर्ष।">🌟 उत्कृष्ट कार्य!</span>
                        <span class="quick-feedback-chip grading-chip" data-id="${sub.id}" data-text="रेखाचित्र एवं किरण आरेख बहुत स्पष्ट और सटीक हैं।">📐 स्पष्ट रेखाचित्र</span>
                        <span class="quick-feedback-chip grading-chip" data-id="${sub.id}" data-text="गणना के चरणों को स्पष्ट रूप से दर्शाएं, उत्तर सही है।">✏️ गणना के चरण स्पष्ट करें</span>
                        <span class="quick-feedback-chip grading-chip" data-id="${sub.id}" data-text="सिद्धांत सही है लेकिन प्रयोगात्मक सावधानियों का भी उल्लेख करें।">⚠️ सावधानियाँ लिखें</span>
                    </div>
                    <textarea id="remarks-input-${sub.id}" rows="2" style="width: 100%; padding: 8px; border: 1px solid var(--border-color); border-radius: 6px; font-size: 13px;" placeholder="Write constructive feedback for student...">${sub.teacherRemarks || ''}</textarea>
                </div>

                <div style="display: flex; justify-content: flex-end; gap: 10px; margin-top: 10px;">
                    ${isGraded ? `
                        <button type="button" class="btn btn-secondary btn-sm cancel-edit-grade-btn" data-id="${sub.id}">Cancel</button>
                    ` : ''}
                    <button type="button" class="btn btn-primary btn-sm save-grade-btn" data-id="${sub.id}">💾 Save Grade &amp; Publish to Student</button>
                </div>
            </div>
        `;
        container.appendChild(card);
    });

    // Wire chips
    document.querySelectorAll('.grading-chip').forEach(chip => {
        chip.addEventListener('click', (e) => {
            const id = e.target.dataset.id;
            const text = e.target.dataset.text;
            const textarea = document.getElementById(`remarks-input-${id}`);
            if (textarea) {
                textarea.value = textarea.value ? `${textarea.value} ${text}` : text;
                textarea.focus();
            }
        });
    });

    // Wire edit grade toggles
    document.querySelectorAll('.toggle-edit-grade-btn').forEach(btn => {
        btn.addEventListener('click', (e) => {
            const id = e.target.dataset.id;
            const box = document.getElementById(`eval-box-${id}`);
            if (box) box.classList.toggle('hidden');
        });
    });

    document.querySelectorAll('.cancel-edit-grade-btn').forEach(btn => {
        btn.addEventListener('click', (e) => {
            const id = e.target.dataset.id;
            const box = document.getElementById(`eval-box-${id}`);
            if (box) box.classList.add('hidden');
        });
    });

    // Wire save grade buttons
    document.querySelectorAll('.save-grade-btn').forEach(btn => {
        btn.addEventListener('click', (e) => {
            const id = e.target.dataset.id;
            const marksEl = document.getElementById(`marks-input-${id}`);
            const letterEl = document.getElementById(`grade-letter-input-${id}`);
            const remarksEl = document.getElementById(`remarks-input-${id}`);

            if (!marksEl || !letterEl || !remarksEl) return;

            const marks = parseFloat(marksEl.value) || 0;
            const letter = letterEl.value;
            const remarks = remarksEl.value.trim() || 'Good effort. Keep progressing!';

            const subs = getStoredSubmissions();
            const target = subs.find(s => s.id === id);
            if (target) {
                target.marks = marks;
                target.gradeLetter = letter;
                target.teacherRemarks = remarks;
                target.status = 'GRADED';
                target.gradedBy = state.currentUser.name || 'Verified Teacher';
                target.gradedAt = Date.now();

                saveSubmissionsToStorage(subs);
                state.submissionsList = subs;

                renderTeacherGrading();
                renderTeacherQuizAnalytics();
                renderStudentAssignments();
                alert(`Grade "${letter}" (${marks}/${target.maxMarks || 10}) successfully published for ${target.studentName}!`);
            }
        });
    });
}


/* DYNAMIC NAVIGATION BAR & PORTAL ISOLATION BASED ON USER ROLE */
function updateNavigationAndRoleUI() {
    const roleName = document.getElementById('auth-role-name');
    const roleType = document.getElementById('auth-role-type');
    const navContainer = document.getElementById('nav-container-tabs');
    const xpWidget = document.getElementById('header-xp-widget');
    const streakWidget = document.getElementById('header-streak-widget');

    if (roleName) roleName.innerText = state.currentUser.name;

    if (state.currentUser.role === 'TEACHER') {
        if (roleType) {
            roleType.innerText = `Verified Teacher (${state.currentUser.teacherId})`;
            roleType.style.color = '#6B21A8';
        }
        if (xpWidget) xpWidget.classList.add('hidden');
        if (streakWidget) streakWidget.classList.add('hidden');

        // Teacher Navigation Bar
        if (navContainer) {
            navContainer.innerHTML = `
                <button class="nav-btn active" data-tab="teacher">
                    <span class="nav-icon">👨‍🏫</span>
                    <span class="nav-text">Teacher Portal & Studio</span>
                </button>
                <button class="nav-btn" data-tab="home">
                    <span class="nav-icon">📚</span>
                    <span class="nav-text">MP Board Modules</span>
                </button>
            `;
        }
    } else {
        if (roleType) {
            roleType.innerText = `Student (${state.currentUser.grade})`;
            roleType.style.color = '#4338CA';
        }
        if (xpWidget) xpWidget.classList.remove('hidden');
        if (streakWidget) streakWidget.classList.remove('hidden');

        // Student Navigation Bar
        if (navContainer) {
            navContainer.innerHTML = `
                <button class="nav-btn active" data-tab="ai-solver">
                    <span class="nav-icon">🤖</span>
                    <span class="nav-text">Live AI Solver & My Doubts</span>
                </button>
                <button class="nav-btn" data-tab="home">
                    <span class="nav-icon">📚</span>
                    <span class="nav-text">MP Board Modules</span>
                </button>
                <button class="nav-btn" data-tab="assignments">
                    <span class="nav-icon">📂</span>
                    <span class="nav-text">Assignments & Projects</span>
                </button>
                <button class="nav-btn" data-tab="puzzle">
                    <span class="nav-icon">🧩</span>
                    <span class="nav-text">Daily Puzzles</span>
                </button>
                <button class="nav-btn" data-tab="opportunities">
                    <span class="nav-icon">🌟</span>
                    <span class="nav-text">Scholarships & Careers</span>
                </button>
            `;
        }
    }

    // Re-bind tab click events
    document.querySelectorAll('.nav-btn').forEach(btn => {
        btn.addEventListener('click', () => switchTab(btn.dataset.tab));
    });
}

function switchTab(tabName) {
    if (tabName === 'teacher' && state.currentUser.role !== 'TEACHER') {
        alert('Access Denied! The Teacher Portal is restricted to verified MP Education Department Teachers only.');
        return;
    }

    state.activeTab = tabName;
    document.querySelectorAll('.nav-btn').forEach(btn => {
        btn.classList.toggle('active', btn.dataset.tab === tabName);
    });
    document.querySelectorAll('.tab-content').forEach(content => {
        content.classList.toggle('active', content.id === `tab-${tabName}`);
    });
}

function renderStudentAssignments() {
    const container = document.getElementById('student-assignments-list');
    if (!container) return;
    container.innerHTML = '';

    const targetGrade = state.selectedAssignmentGrade || (state.currentUser ? state.currentUser.grade : 'Class 8');
    const targetSubject = state.selectedAssignmentSubject || 'All';

    const allAssignments = getStoredAssignments();
    const filtered = allAssignments.filter(a => {
        const matchGrade = (!a.grade || a.grade === targetGrade);
        const matchSubject = (targetSubject === 'All' || a.subject === targetSubject);
        return matchGrade && matchSubject;
    });

    if (filtered.length === 0) {
        container.innerHTML = `<p style="color: var(--text-secondary); padding: 20px; text-align: center;">No active assignments for ${targetGrade} (${targetSubject}) currently.</p>`;
        return;
    }

    const allSubs = getStoredSubmissions();
    const currentUsername = (state.currentUser ? state.currentUser.username : 'aarav').toLowerCase();

    filtered.forEach(a => {
        const card = document.createElement('div');
        card.className = 'card';
        card.style.marginBottom = '16px';

        let typeBadgeClass = 'mastery-badge mastered';
        if (a.type === 'Hands-on Project') typeBadgeClass = 'mastery-badge developing';
        if (a.type === 'Open Question') typeBadgeClass = 'mastery-badge weak';

        // Check if current student has a submission
        const studentSub = allSubs.find(s => s.assignmentId === a.id && s.studentId.toLowerCase() === currentUsername);

        card.innerHTML = `
            <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 8px;">
                <span class="subject-badge">${a.grade} • ${a.subject}</span>
                <span class="${typeBadgeClass}">${a.type}</span>
            </div>
            <h3 style="font-size: 16px; margin-bottom: 8px;">${a.title}</h3>
            <p style="font-size: 14px; color: var(--text-secondary); margin-bottom: 12px; background: #F8FAFC; padding: 10px; border-radius: 6px;">${a.instructions}</p>
            
            ${studentSub ? `
                <div style="margin-bottom: 12px;">
                    ${studentSub.status === 'GRADED' ? `
                        <div style="background: var(--forest-green-light); border: 1px solid var(--forest-green); border-radius: 8px; padding: 12px;">
                            <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 4px;">
                                <strong style="color: var(--forest-green); font-size: 14px;">🏆 Teacher Evaluation: Grade ${studentSub.gradeLetter} (${studentSub.marks}/${studentSub.maxMarks || 10} Marks)</strong>
                                <span class="status-badge-inline resolved">✓ Graded (+50 XP)</span>
                            </div>
                            <div style="font-size: 13px; color: var(--text-primary); margin-top: 4px;">
                                👨‍🏫 <strong>Teacher Feedback (${studentSub.gradedBy || 'Teacher'}):</strong> "${studentSub.teacherRemarks}"
                            </div>
                            <div style="font-size: 12px; color: var(--text-secondary); margin-top: 6px; font-style: italic;">
                                Your Answer: "${studentSub.content}"
                            </div>
                        </div>
                    ` : `
                        <div style="background: #FFF3E6; border: 1px solid #FED7AA; border-radius: 8px; padding: 12px;">
                            <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 4px;">
                                <strong style="color: var(--primary-saffron-dark); font-size: 13.5px;">⏳ Submitted — Awaiting Teacher Evaluation &amp; Marks</strong>
                                <span class="status-badge-inline resolved">✓ Submitted</span>
                            </div>
                            <div style="font-size: 12px; color: var(--text-secondary); margin-top: 4px;">
                                Your Answer: "${studentSub.content}"
                            </div>
                        </div>
                    `}
                </div>
            ` : ''}

            <div style="display: flex; justify-content: space-between; align-items: center; flex-wrap: wrap; gap: 8px;">
                <span style="font-size: 12px; color: var(--weak-red); font-weight: 700;">📅 Due Date: ${a.dueDate} • 🕒 ${formatTimestamp(a.timestamp)}</span>
                ${studentSub ? `
                    <button class="btn btn-secondary btn-sm open-submit-modal-btn" data-id="${a.id}" data-title="${a.title}" data-instructions="${a.instructions}">✍️ Resubmit / Update Notes</button>
                ` : `
                    <button class="btn btn-primary btn-sm open-submit-modal-btn" data-id="${a.id}" data-title="${a.title}" data-instructions="${a.instructions}">✍️ Submit Answer / Project</button>
                `}
            </div>
        `;
        container.appendChild(card);
    });

    document.querySelectorAll('.open-submit-modal-btn').forEach(btn => {
        btn.addEventListener('click', (e) => {
            const id = e.target.dataset.id;
            const title = e.target.dataset.title;
            const instructions = e.target.dataset.instructions;

            const modal = document.getElementById('submit-assignment-modal');
            if (modal) {
                document.getElementById('modal-submit-assign-id').value = id;
                document.getElementById('modal-submit-title').innerText = `Submit: ${title}`;
                document.getElementById('modal-submit-instructions').innerText = instructions;
                document.getElementById('modal-submit-response').value = '';
                document.getElementById('modal-submit-attachment-note').value = '';
                modal.classList.remove('hidden');
            }
        });
    });
}

function renderAll() {
    updateNavigationAndRoleUI();
    renderHeaderStats();
    renderLevels();
    initQuiz();
    renderPuzzle();
    renderStudentAssignments();
    renderStudentPushedDoubtsTracker();
    renderTeacherPortal();
    renderTeacherQuizAnalytics();
    renderTeacherGrading();
    renderScholarshipsAndCareers();
}


function renderHeaderStats() {
    const nameEl = document.getElementById('header-profile-name');
    const gradeEl = document.getElementById('header-profile-grade');
    const xpEl = document.getElementById('header-xp');
    const streakEl = document.getElementById('header-streak');

    if (nameEl) nameEl.innerText = state.currentUser.name;
    if (gradeEl) gradeEl.innerText = state.currentUser.grade;
    if (xpEl) xpEl.innerText = `${state.currentUser.xp || 520} XP`;
    if (streakEl) streakEl.innerText = `${state.currentUser.streakDays || 7} Days`;
}

/* UNIVERSAL MP BOARD AI SOLVER ENGINE */
function generateUniversalAISolution(q) {
    const qTrim = q.trim();
    const qLower = qTrim.toLowerCase();

    // 1. Math Algebraic Equations
    const eqMatch = qTrim.match(/(\d*)\s*x\s*([\+\-])\s*(\d+)\s*=\s*(\d+)/i);
    if (eqMatch) {
        let a = parseInt(eqMatch[1] || '1');
        let sign = eqMatch[2];
        let b = parseInt(eqMatch[3]);
        let c = parseInt(eqMatch[4]);

        let step2Val = (sign === '+') ? (c - b) : (c + b);
        let xVal = step2Val / a;

        return {
            query: qTrim,
            source: "MPBSE Class 8 Mathematics — Chapter 2 (Linear Equations)",
            title: `गणित हल (MP Board Math): ${qTrim}`,
            steps: [
                { num: "चरण 1 (समीकरण विश्लेषण)", text: `दिए गए समीकरण ${qTrim} में चर x को बाईं ओर रखकर अचर पद (${sign}${b}) का पक्षांतरण (Transposition) करें।` },
                { num: "चरण 2 (पक्षांतरण नियम)", text: `${a}x = ${c} ${sign === '+' ? '-' : '+'} ${b} ➔ ${a}x = ${step2Val}` },
                { num: "चरण 3 (विभाजन)", text: `दोनों पक्षों में ${a} का भाग दें ➔ x = ${step2Val} / ${a} ➔ x = ${xVal}` },
                { num: "चरण 4 (अंतिम उत्तर)", text: `अतः चर x का सही मान ${xVal} है।` }
            ],
            shortSummary: `${qTrim} ➔ x = ${xVal}`
        };
    }

    // 2. Science - Light & Reflection
    if (qLower.includes('प्रकाश') || qLower.includes('परावर्तन') || qLower.includes('reflection') || qLower.includes('light') || qLower.includes('दर्पण')) {
        return {
            query: qTrim,
            source: "MPBSE Class 8 Science — Chapter 16 (Light)",
            title: `विज्ञान व्याख्या (MP Board Science): ${qTrim}`,
            steps: [
                { num: "मुख्य परिभाषा", text: "जब प्रकाश किरण किसी चिकनी चमकदार सतह (दर्पण) से टकराकर उसी माध्यम में वापस लौटती है, तो इस परिघटना को प्रकाश का परावर्तन कहते हैं।" },
                { num: "नियम 1 (आपतन व परावर्तन कोण)", text: "आपतन कोण (Angle of Incidence, ∠i) और परावर्तन कोण (Angle of Reflection, ∠r) सदैव समान होते हैं (∠i = ∠r)।" },
                { num: "नियम 2 (समतल अभिलंब)", text: "आपतित किरण, परावर्तित किरण तथा आपतन बिंदु पर खींचा गया अभिलंब तीनों एक ही समतल में स्थित होते हैं।" }
            ],
            shortSummary: "प्रकाश का सतह से टकराकर लौटना परावर्तन है (∠i = ∠r)।"
        };
    }

    // 3. Science - Photosynthesis
    if (qLower.includes('प्रकाश संश्लेषण') || qLower.includes('photosynthesis') || qLower.includes('पौधे')) {
        return {
            query: qTrim,
            source: "MPBSE Class 7 Science — Chapter 1",
            title: `जीव विज्ञान (Biology): ${qTrim}`,
            steps: [
                { num: "प्रक्रिया की परिभाषा", text: "हरे पौधे सूर्य के प्रकाश तथा क्लोरोफिल की उपस्थिति में मृदा से जल और वायुमंडल से CO2 लेकर अपना भोजन (ग्लूकोज) बनाते हैं।" },
                { num: "रासायनिक समीकरण", text: "6CO2 + 6H2O + सूर्य प्रकाश + क्लोरोफिल ➔ C6H12O6 (ग्लूकोज) + 6O2 (ऑक्सीजन)" }
            ],
            shortSummary: "पौधे जल, CO2 और सूर्य प्रकाश से ग्लूकोज और O2 बनाते हैं।"
        };
    }

    // 4. Default Fallback
    return {
        query: qTrim,
        source: "MP Board of Secondary Education (MPBSE) RAG Engine",
        title: `एआई समाधान (MP Board Answer): ${qTrim}`,
        steps: [
            { num: "चरण 1 (पाठ्यक्रम संदर्भ)", text: `प्रश्न "${qTrim}" का विश्लेषण मध्य प्रदेश माध्यमिक शिक्षा मंडल पाठ्यक्रम के आधार पर किया गया है।` },
            { num: "चरण 2 (मुख्य अवधारणा)", text: "दिए गए विषय के अनुसार मुख्य नियमों, परिभाषाओं एवं सिद्धांतों को लागू कर चरणबद्ध उत्तर निकाला गया है।" },
            { num: "चरण 3 (सलाह)", text: "यदि आपको इसमें कोई शंका है तो नीचे दिए गए 'Push to Teacher' बटन द्वारा अपने शिक्षक को भेज सकते हैं।" }
        ],
        shortSummary: `प्रश्न "${qTrim}" का उत्तर एआई द्वारा तैयार किया गया है।`
    };
}

/* 2. LIVE ONLINE AI QUESTION SOLVER TAB */
async function handleLiveAISolve() {
    const inputEl = document.getElementById('ai-question-input');
    if (!inputEl) return;
    const query = inputEl.value.trim();
    if (!query) return;

    const emptyBox = document.getElementById('ai-empty-state');
    const solutionBox = document.getElementById('ai-solution-content');
    const solveBtn = document.getElementById('ai-solve-btn');

    if (emptyBox) emptyBox.classList.add('hidden');
    if (solutionBox) solutionBox.classList.remove('hidden');

    const titleEl = document.getElementById('solution-title');
    const sourceEl = document.getElementById('ai-source-tag');
    const stepsContainer = document.getElementById('solution-steps');

    // Show loading state
    if (titleEl) titleEl.innerText = '⏳ Gemini AI is solving your question...';
    if (sourceEl) sourceEl.innerText = 'Powered by Google Gemini AI • MP Board Curriculum';
    if (stepsContainer) stepsContainer.innerHTML = `
        <div class="step-card" style="text-align: center; padding: 24px;">
            <div style="font-size: 28px; margin-bottom: 8px;">🤖</div>
            <div style="color: var(--text-secondary);">Generating step-by-step MP Board solution with Gemini AI...</div>
        </div>`;
    if (solveBtn) { solveBtn.disabled = true; solveBtn.innerText = '⏳ Solving...'; }

    const grade = state.currentUser?.grade || 'Class 8';
    const geminiPrompt = `You are an expert MP Board (MPBSE) education assistant for students in ${grade}.
Answer the following student question with step-by-step explanation in HINDI and English (bilingual).
Be clear, encouraging and curriculum-appropriate for a rural MP student.

Question: ${query}

Format your response as:
**शीर्षक (Title):** [Brief topic title]
**स्रोत (Source):** [MPBSE Chapter/Book reference]

**चरण 1 (Step 1):** [First step explanation]
**चरण 2 (Step 2):** [Second step]
**चरण 3 (Step 3):** [Third step if needed]

**सार (Summary):** [One line summary in Hindi]`;

    const geminiText = await callGeminiAPI(geminiPrompt);

    if (geminiText) {
        // Parse Gemini response
        const titleMatch = geminiText.match(/\*\*शीर्षक.*?:\*\*\s*(.+)/i) || geminiText.match(/\*\*Title.*?:\*\*\s*(.+)/i);
        const sourceMatch = geminiText.match(/\*\*स्रोत.*?:\*\*\s*(.+)/i) || geminiText.match(/\*\*Source.*?:\*\*\s*(.+)/i);
        const summaryMatch = geminiText.match(/\*\*सार.*?:\*\*\s*(.+)/i) || geminiText.match(/\*\*Summary.*?:\*\*\s*(.+)/i);

        const steps = [];
        const stepRegex = /\*\*(?:चरण|Step)\s*(\d+).*?:\*\*\s*([^*]+)/gi;
        let match;
        while ((match = stepRegex.exec(geminiText)) !== null) {
            steps.push({ num: `चरण ${match[1]}`, text: match[2].trim() });
        }
        if (steps.length === 0) {
            // Fallback: split by newlines and show as steps
            const lines = geminiText.split('\n').filter(l => l.trim().length > 20);
            lines.slice(0, 5).forEach((line, i) => steps.push({ num: `चरण ${i+1}`, text: line.replace(/\*\*/g, '').trim() }));
        }

        const sol = {
            query: query,
            source: sourceMatch ? sourceMatch[1].trim() : `MPBSE ${grade} Curriculum — Powered by Gemini AI`,
            title: titleMatch ? titleMatch[1].trim() : `Gemini AI Solution: ${query}`,
            steps: steps.length > 0 ? steps : [{ num: 'उत्तर', text: geminiText.replace(/\*\*/g, '').trim() }],
            shortSummary: summaryMatch ? summaryMatch[1].trim() : query
        };
        state.currentAISolution = sol;

        if (titleEl) titleEl.innerText = sol.title;
        if (sourceEl) sourceEl.innerText = `🤖 ${sol.source}`;
        if (stepsContainer) {
            stepsContainer.innerHTML = sol.steps.map(s => `
                <div class="step-card">
                    <div class="step-number">${s.num}</div>
                    <div>${s.text}</div>
                </div>
            `).join('');
        }
    } else {
        // Fallback to local engine
        const sol = generateUniversalAISolution(query);
        state.currentAISolution = sol;
        if (titleEl) titleEl.innerText = sol.title;
        if (sourceEl) sourceEl.innerText = sol.source;
        if (stepsContainer) {
            stepsContainer.innerHTML = sol.steps.map(s => `
                <div class="step-card">
                    <div class="step-number">${s.num}</div>
                    <div>${s.text}</div>
                </div>
            `).join('');
        }
    }

    if (solveBtn) { solveBtn.disabled = false; solveBtn.innerText = '🔍 Solve with AI'; }
}

/* 3. ESCALATE AI SOLUTION TO TEACHER & RENDER STUDENT DOUBTS TRACKER */
function handleEscalateAISolutionToTeacher() {
    if (!state.currentAISolution) {
        alert('Please ask a question first to generate an AI solution!');
        return;
    }

    const sol = state.currentAISolution;

    const newDoubt = {
        id: 'd_' + Date.now(),
        studentName: state.currentUser.name,
        studentId: state.currentUser.username || 'student',
        questionText: sol.query,
        timestamp: Date.now(),
        aiSolutionObj: sol,
        isEscalatedToTeacher: true,
        teacherReply: null
    };

    state.doubtsList.unshift(newDoubt);
    saveDoubtsToStorage(state.doubtsList);

    const statusMsg = document.getElementById('escalation-status-msg');
    if (statusMsg) statusMsg.classList.remove('hidden');

    renderStudentPushedDoubtsTracker();
    renderTeacherPortal();
}

function renderStudentPushedDoubtsTracker() {
    const trackerContainer = document.getElementById('student-pushed-doubts-list');
    if (!trackerContainer) return;
    trackerContainer.innerHTML = '';

    const studentPushed = state.doubtsList.filter(d => d.isEscalatedToTeacher);

    if (studentPushed.length === 0) {
        trackerContainer.innerHTML = '<p style="font-size: 13px; color: var(--text-secondary);">No doubts pushed to teacher yet.</p>';
        return;
    }

    studentPushed.forEach(d => {
        const sol = d.aiSolutionObj || generateUniversalAISolution(d.questionText);
        const card = document.createElement('div');
        card.className = 'doubt-card-item';
        card.style.marginBottom = '12px';

        card.innerHTML = `
            <div style="font-size: 15px; font-weight: 700; margin-bottom: 6px;">Q: ${d.questionText}</div>
            <div style="font-size: 12px; color: var(--text-secondary); margin-bottom: 8px;">AI Provided: ${sol.shortSummary}</div>
            
            ${d.teacherReply ? `
                <div style="background: var(--forest-green-light); color: var(--forest-green); padding: 10px; border-radius: 6px; font-size: 13px; font-weight: 600;">
                    👨‍🏫 <strong>Teacher Reply:</strong> ${d.teacherReply}
                </div>
            ` : `
                <div style="background: #FFF3E6; color: var(--primary-saffron-dark); padding: 8px; border-radius: 6px; font-size: 12px; font-weight: 700;">
                    ⏳ Pending Teacher Review (शिक्षक के उत्तर की प्रतीक्षा है...)
                </div>
            `}
        `;
        trackerContainer.appendChild(card);
    });
}

function renderTeacherPortal() {
    const container = document.getElementById('teacher-doubt-items');
    if (!container) return;
    container.innerHTML = '';

    // 1. Dynamic Enrolled Students Count
    const enrolledStudents = getEnrolledStudentsRoster();
    const enrolledCountEl = document.getElementById('teacher-enrolled-students-count');
    if (enrolledCountEl) {
        enrolledCountEl.innerText = enrolledStudents.length;
    }

    // 2. Doubts statistics
    const allDoubts = state.doubtsList.filter(d => d.isEscalatedToTeacher);
    const pendingDoubts = allDoubts.filter(d => !d.teacherReply);
    const resolvedDoubts = allDoubts.filter(d => d.teacherReply);

    const pendingCountEl = document.getElementById('escalated-count-stat');
    if (pendingCountEl) pendingCountEl.innerText = pendingDoubts.length;

    const resolvedCountEl = document.getElementById('resolved-doubts-stat');
    if (resolvedCountEl) resolvedCountEl.innerText = resolvedDoubts.length;

    const classAvgEl = document.getElementById('class-avg-mastery-stat');
    if (classAvgEl && enrolledStudents.length > 0) {
        const avg = Math.round(enrolledStudents.reduce((acc, s) => acc + (s.avgScore || 75), 0) / enrolledStudents.length);
        classAvgEl.innerText = `${avg}%`;
    }

    // Filter Badges
    const badgeAll = document.getElementById('inbox-count-all');
    if (badgeAll) badgeAll.innerText = allDoubts.length;
    const badgePending = document.getElementById('inbox-count-pending');
    if (badgePending) badgePending.innerText = pendingDoubts.length;
    const badgeResolved = document.getElementById('inbox-count-resolved');
    if (badgeResolved) badgeResolved.innerText = resolvedDoubts.length;

    // Filter Doubts
    let filteredDoubts = allDoubts;
    if (state.inboxFilterStatus === 'PENDING') {
        filteredDoubts = pendingDoubts;
    } else if (state.inboxFilterStatus === 'RESOLVED') {
        filteredDoubts = resolvedDoubts;
    }

    if (state.inboxGradeFilter && state.inboxGradeFilter !== 'All') {
        filteredDoubts = filteredDoubts.filter(d => (d.grade || 'Class 8') === state.inboxGradeFilter);
    }

    if (state.inboxSearchQuery) {
        const q = state.inboxSearchQuery;
        filteredDoubts = filteredDoubts.filter(d => 
            (d.studentName && d.studentName.toLowerCase().includes(q)) ||
            (d.questionText && d.questionText.toLowerCase().includes(q))
        );
    }

    if (filteredDoubts.length === 0) {
        container.innerHTML = `
            <div style="text-align: center; padding: 24px; color: var(--text-secondary); background: #F8FAFC; border-radius: var(--radius-md); border: 1px dashed var(--border-color);">
                <span style="font-size: 24px; display: block; margin-bottom: 6px;">🎉</span>
                <strong>No student inquiries found matching current filter.</strong>
                <p style="font-size: 12px; margin-top: 4px;">All escalated doubts have been reviewed, or no doubts match your search query.</p>
            </div>
        `;
        return;
    }

    filteredDoubts.forEach(d => {
        const box = document.createElement('div');
        box.className = 'teacher-doubt-box';
        const sol = d.aiSolutionObj || generateUniversalAISolution(d.questionText);
        const studentInfo = enrolledStudents.find(s => s.username === d.studentId) || {};
        const studentGrade = d.grade || studentInfo.grade || 'Class 8';
        const studentSchool = d.school || studentInfo.school || 'Govt School, MP';

        box.innerHTML = `
            <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 6px; flex-wrap: wrap; gap: 6px;">
                <div style="display: flex; align-items: center; gap: 8px;">
                    <span style="font-size: 14px; font-weight: 700; color: var(--text-primary);">🧑‍🎓 ${d.studentName}</span>
                    <span class="subject-badge" style="font-size: 11px;">${studentGrade} • ${studentSchool}</span>
                </div>
                <span style="font-size: 11px; color: var(--text-secondary); font-weight: 600;">🕒 ${formatTimestamp(d.timestamp)}</span>
            </div>

            <div style="font-size: 15px; font-weight: 700; color: var(--text-primary); margin-bottom: 6px;">
                ❓ ${d.questionText}
            </div>

            <div style="font-size: 12.5px; color: var(--text-secondary); margin-bottom: 12px; background: white; padding: 10px; border-radius: 6px; border: 1px solid var(--border-color); line-height: 1.5;">
                <strong style="color: #0369A1;">🤖 AI Solution Summary Provided to Student:</strong><br>
                ${sol.shortSummary || sol.steps.map(s => s.text).join(' ')}
            </div>

            ${d.teacherReply ? `
                <div style="background: var(--forest-green-light); border: 1px solid var(--forest-green); color: var(--forest-green); padding: 12px; border-radius: 8px; font-size: 13px;">
                    <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 4px;">
                        <strong>👨‍🏫 Verified Teacher Reply Sent:</strong>
                        <button class="btn btn-secondary btn-sm edit-doubt-reply-btn" data-id="${d.id}" style="font-size: 11px; padding: 2px 8px; height: auto;">✏️ Edit Reply</button>
                    </div>
                    <div style="color: var(--text-primary); font-weight: 500;">${d.teacherReply}</div>
                    <div style="font-size: 11px; color: var(--forest-green); margin-top: 4px; font-weight: 600;">✓ Delivered to Student Portal • ${formatTimestamp(d.repliedAt || d.timestamp)}</div>
                </div>
                <div id="edit-reply-box-${d.id}" class="hidden" style="margin-top: 10px;">
                    <textarea class="teacher-reply-input" id="reply-input-${d.id}" rows="2">${d.teacherReply}</textarea>
                    <button class="btn btn-primary btn-sm send-reply-btn" data-id="${d.id}">Update Reply</button>
                </div>
            ` : `
                <div style="background: #FFFDF9; border: 1px solid #FED7AA; padding: 12px; border-radius: 8px;">
                    <div style="font-size: 12px; font-weight: 700; color: var(--primary-saffron-dark); margin-bottom: 6px;">
                        ✍️ Quick Teacher Guidance Response:
                    </div>
                    <div style="margin-bottom: 8px;">
                        <span class="quick-feedback-chip doubt-chip" data-id="${d.id}" data-text="शाबाश! आपने सही प्रश्न पूछा है।">🌟 शाबाश!</span>
                        <span class="quick-feedback-chip doubt-chip" data-id="${d.id}" data-text="नियम ध्यान रखें: आपतन कोण हमेशा परावर्तन कोण के बराबर होता है (∠i = ∠r)।">📐 नियम: ∠i = ∠r</span>
                        <span class="quick-feedback-chip doubt-chip" data-id="${d.id}" data-text="समीकरण में पहले संख्या का पक्षांतरण करें, फिर x का गुणांक भाग में ले जाएँ।">✏️ पक्षांतरण नियम</span>
                        <span class="quick-feedback-chip doubt-chip" data-id="${d.id}" data-text="कक्षा में कल इस पर विशेष प्रयोग करके समझेंगे।">🏫 कक्षा में चर्चा</span>
                    </div>
                    <textarea class="teacher-reply-input" id="reply-input-${d.id}" rows="2" placeholder="Type personal teacher answer or click any quick suggestion above..."></textarea>
                    <div style="display: flex; justify-content: flex-end; gap: 8px; margin-top: 6px;">
                        <button class="btn btn-primary btn-sm send-reply-btn" data-id="${d.id}">🚀 Send Verified Answer to Student</button>
                    </div>
                </div>
            `}
        `;
        container.appendChild(box);
    });

    // Wire chips
    document.querySelectorAll('.doubt-chip').forEach(chip => {
        chip.addEventListener('click', (e) => {
            const id = e.target.dataset.id;
            const text = e.target.dataset.text;
            const input = document.getElementById(`reply-input-${id}`);
            if (input) {
                input.value = input.value ? `${input.value} ${text}` : text;
                input.focus();
            }
        });
    });

    // Wire edit reply buttons
    document.querySelectorAll('.edit-doubt-reply-btn').forEach(btn => {
        btn.addEventListener('click', (e) => {
            const id = e.target.dataset.id;
            const box = document.getElementById(`edit-reply-box-${id}`);
            if (box) box.classList.toggle('hidden');
        });
    });

    // Wire send reply buttons
    document.querySelectorAll('.send-reply-btn').forEach(btn => {
        btn.addEventListener('click', (e) => {
            const id = e.target.dataset.id;
            const replyInput = document.getElementById(`reply-input-${id}`);
            if (replyInput) {
                const text = replyInput.value.trim();
                if (text) {
                    const doubt = state.doubtsList.find(d => d.id === id);
                    if (doubt) {
                        doubt.teacherReply = text;
                        doubt.repliedAt = Date.now();
                    }
                    saveDoubtsToStorage(state.doubtsList);
                    renderTeacherPortal();
                    renderStudentPushedDoubtsTracker();
                    alert('Teacher reply sent directly to student portal!');
                } else {
                    alert('Please enter a reply before sending!');
                }
            }
        });
    });
}


/* SPEECH STT & TTS */
function handleSpeechInput(targetId) {
    if (!('webkitSpeechRecognition' in window) && !('SpeechRecognition' in window)) {
        alert('Speech STT simulation active! Sample loaded.');
        document.getElementById(targetId).value = "2x + 5 = 15 में x का मान";
        if (targetId === 'ai-question-input') handleLiveAISolve();
        return;
    }

    const SpeechRecognition = window.SpeechRecognition || window.webkitSpeechRecognition;
    const recognition = new SpeechRecognition();
    recognition.lang = 'hi-IN';
    recognition.start();

    recognition.onresult = (event) => {
        const transcript = event.results[0][0].transcript;
        document.getElementById(targetId).value = transcript;
        if (targetId === 'ai-question-input') handleLiveAISolve();
    };
}

function speakTextHindi(text) {
    if (!('speechSynthesis' in window)) return;
    const utterance = new SpeechSynthesisUtterance(text);
    utterance.lang = 'hi-IN';
    window.speechSynthesis.speak(utterance);
}

/* MP BOARD CURRICULUM MODULES (WITH TEACHER REMOVE BUTTON) */
function renderLevels() {
    const grid = document.getElementById('levels-grid');
    if (!grid) return;
    grid.innerHTML = '';

    const customModules = getStoredModules();
    const allCombined = [...state.mpBoardModules, ...customModules];

    const filtered = allCombined.filter(m => {
        const matchGrade = (m.grade === state.selectedModuleGrade);
        const matchSub = (state.selectedModuleSubject === 'All' || m.subject === state.selectedModuleSubject);
        return matchGrade && matchSub;
    });

    if (filtered.length === 0) {
        grid.innerHTML = '<p style="grid-column: 1/-1; color: var(--text-secondary); text-align: center; padding: 20px;">No modules found for selected Grade/Subject combination. Try selecting "All Subjects" or a different Class.</p>';
        return;
    }

    filtered.forEach(level => {
        const isCustom = level.id.startsWith('m_custom_');
        const card = document.createElement('div');
        card.className = 'card level-card';
        card.innerHTML = `
            <div>
                <div class="level-card-header">
                    <span class="subject-badge">${level.grade} • ${level.subject}</span>
                    <span class="mastery-badge ${isCustom ? 'developing' : 'mastered'}">${isCustom ? '👨‍🏫 Teacher Created' : 'MPBSE Approved'}</span>
                </div>
                <div style="font-size: 11px; color: var(--text-secondary); margin-bottom: 6px; font-weight: 600;">
                    🕒 ${formatTimestamp(level.timestamp)}
                </div>
                <h3 class="level-title-hi">${level.titleHindi}</h3>
                <p class="level-title-en">${level.titleEnglish}</p>
            </div>
            <div>
                <button class="btn btn-primary btn-block start-lesson-btn" data-id="${level.id}">
                    📖 Open ${level.subject} Study Material
                </button>
            </div>
        `;
        grid.appendChild(card);
    });

    document.querySelectorAll('.start-lesson-btn').forEach(btn => {
        btn.addEventListener('click', (e) => {
            const id = e.target.closest('button').dataset.id;
            openLessonModal(id);
        });
    });
}

function openLessonModal(levelId) {
    const customModules = getStoredModules();
    const allCombined = [...state.mpBoardModules, ...customModules];
    const level = allCombined.find(l => l.id === levelId);
    if (!level) return;

    const modal = document.getElementById('lesson-modal');
    const body = document.getElementById('lesson-modal-body');

    if (body) {
        body.innerHTML = `
            <h2>${level.titleHindi}</h2>
            <p style="color: var(--text-secondary); margin-bottom: 12px;">MP Board ${level.grade} Syllabus Material (${level.subject})</p>
            <div style="display: flex; flex-direction: column; gap: 12px; margin-top: 14px;">
                ${level.lessons.map(lesson => `
                    <div style="background: #F8FAFC; padding: 14px; border-radius: 8px; border: 1px solid var(--border-color);">
                        <h4>${lesson.title}</h4>
                        <p style="font-size: 14px; color: var(--text-secondary);">${lesson.content}</p>
                    </div>
                `).join('')}
            </div>
        `;
    }

    if (modal) modal.classList.remove('hidden');
}

/* QUIZ ENGINE */
let currentQuestionIndex = 0;
let userAnswers = [];

function initQuiz() {
    currentQuestionIndex = 0;
    userAnswers = [];
    showQuestion();
}

function getDailyAIQuizBank() {
    const targetGrade = state.currentUser ? state.currentUser.grade : 'Class 8';
    const customModules = getStoredModules();
    const allModules = [...state.mpBoardModules, ...customModules];
    
    const now = Date.now();
    const threeDaysMs = 3 * 24 * 60 * 60 * 1000;
    
    let recentModules = allModules.filter(m => m.grade === targetGrade && (now - (m.timestamp || now)) <= threeDaysMs);
    if (recentModules.length === 0) {
        recentModules = allModules.filter(m => m.grade === targetGrade);
    }
    
    let generatedQuestions = [];
    
    recentModules.forEach((m) => {
        const lessonTitle = m.lessons && m.lessons.length > 0 ? m.lessons[0].title : m.titleHindi;
        const subjectName = m.subject;
        
        generatedQuestions.push({
            questionText: `[Daily AI Quiz - Past 3 Days Content (${m.grade} ${subjectName})]: अध्याय "${m.titleHindi}" का मुख्य विषय क्या है?`,
            options: [
                `अवधारणा: ${lessonTitle} के मुख्य सिद्धांतों को समझना।`,
                `केवल आरेख बनाना।`,
                `केवल परिभाषा याद रखना।`,
                `उपरोक्त में से कोई नहीं।`
            ],
            correctAnswerIndex: 0,
            explanation: `यह प्रश्न पिछले 3 दिनों के ${m.grade} मॉड्यूल "${m.titleHindi}" से एआई द्वारा स्वतः संश्लेषित किया गया है।`
        });
    });

    if (generatedQuestions.length < 3) {
        generatedQuestions.push(
            { questionText: `[Daily AI - ${targetGrade} Science]: प्रकाश के परावर्तन में आपतन कोण और परावर्तन कोण में क्या संबंध होता है?`, options: ['∠i = ∠r (सदैव समान)', '∠i > ∠r', '∠i < ∠r', 'कोई संबंध नहीं'], correctAnswerIndex: 0, explanation: 'परावर्तन के नियम के अनुसार ∠i = ∠r।' },
            { questionText: `[Daily AI - ${targetGrade} Math]: समीकरण 2x + 5 = 15 में चर x का मान क्या है?`, options: ['5', '10', '15', '2'], correctAnswerIndex: 0, explanation: '2x = 10 ➔ x = 5।' },
            { questionText: `[Daily AI - ${targetGrade} Social Science]: भारतीय संविधान किस वर्ष लागू हुआ था?`, options: ['26 जनवरी 1950', '15 अगस्त 1947', '26 नवंबर 1949', '2 अक्टूबर 1950'], correctAnswerIndex: 0, explanation: '26 जनवरी 1950 को संविधान लागू हुआ।' }
        );
    }

    return generatedQuestions;
}

function getCombinedQuizBank(subject) {
    if (subject === 'Auto-AI-Daily') {
        return getDailyAIQuizBank();
    }

    const defaultBank = state.subjectQuizBank[subject] || state.subjectQuizBank['Science'];
    const customQuizSets = getStoredQuizQuestions();
    const targetGrade = state.currentUser ? state.currentUser.grade : 'Class 8';
    
    let matchingCustomQs = [];
    customQuizSets.forEach(set => {
        if (set.subject === subject && (!set.grade || set.grade === targetGrade)) {
            matchingCustomQs.push(...set.questions);
        }
    });

    return [...defaultBank, ...matchingCustomQs];
}

function showQuestion() {
    const qBox = document.getElementById('quiz-box');
    const rBox = document.getElementById('quiz-results');
    if (qBox) qBox.classList.remove('hidden');
    if (rBox) rBox.classList.add('hidden');

    const bank = getCombinedQuizBank(state.selectedQuizSubject);
    if (!bank || bank.length === 0) {
        const questionEl = document.getElementById('quiz-question-text');
        if (questionEl) questionEl.innerText = "No questions available for this subject yet.";
        return;
    }

    if (currentQuestionIndex >= bank.length) currentQuestionIndex = 0;
    const q = bank[currentQuestionIndex];

    const progressEl = document.getElementById('quiz-progress-text');
    const questionEl = document.getElementById('quiz-question-text');

    if (progressEl) progressEl.innerText = `Question ${currentQuestionIndex + 1} of ${bank.length}`;
    if (questionEl) questionEl.innerText = q.questionText;

    const optionsBox = document.getElementById('quiz-options');
    if (optionsBox) {
        optionsBox.innerHTML = '';
        q.options.forEach((optText, index) => {
            const div = document.createElement('div');
            div.className = 'quiz-option';
            div.innerText = optText;
            div.addEventListener('click', () => selectAnswer(index));
            optionsBox.appendChild(div);
        });
    }

    const exp = document.getElementById('quiz-explanation');
    if (exp) exp.classList.add('hidden');

    const nextBtn = document.getElementById('next-q-btn');
    if (nextBtn) nextBtn.disabled = true;
}

function selectAnswer(selectedIndex) {
    const bank = getCombinedQuizBank(state.selectedQuizSubject);
    const q = bank[currentQuestionIndex];
    const options = document.querySelectorAll('.quiz-option');

    options.forEach((opt, idx) => {
        opt.style.pointerEvents = 'none';
        if (idx === q.correctAnswerIndex) {
            opt.classList.add('correct');
        } else if (idx === selectedIndex) {
            opt.classList.add('incorrect');
        }
    });

    userAnswers[currentQuestionIndex] = (selectedIndex === q.correctAnswerIndex);
    recordQuizAnswerToAnalytics(state.selectedQuizSubject, q, selectedIndex);

    const exp = document.getElementById('quiz-explanation');
    if (exp) {
        exp.innerText = `व्याख्या (Explanation): ${q.explanation}`;
        exp.classList.remove('hidden');
    }

    const nextBtn = document.getElementById('next-q-btn');
    if (nextBtn) {
        nextBtn.disabled = false;
        nextBtn.onclick = handleNextQuestion;
    }
}

function handleNextQuestion() {
    const bank = getCombinedQuizBank(state.selectedQuizSubject);
    currentQuestionIndex++;
    if (currentQuestionIndex < bank.length) {
        showQuestion();
    } else {
        finishQuiz();
    }
}

function finishQuiz() {
    const qBox = document.getElementById('quiz-box');
    const rBox = document.getElementById('quiz-results');
    if (qBox) qBox.classList.add('hidden');
    if (rBox) rBox.classList.remove('hidden');

    const bank = getCombinedQuizBank(state.selectedQuizSubject);
    const correctCount = userAnswers.filter(a => a).length;
    const scorePct = Math.round((correctCount / bank.length) * 100);

    const scoreEl = document.getElementById('result-score-text');
    if (scoreEl) scoreEl.innerText = `Score: ${scorePct}%`;

    const summaryBox = document.getElementById('result-mastery-box');
    if (summaryBox) summaryBox.innerHTML = `<span class="mastery-badge mastered">Mastered (${scorePct}%)</span>`;

    state.currentUser.xp = (state.currentUser.xp || 500) + (scorePct * 2);
    localStorage.setItem('lq_current_user', JSON.stringify(state.currentUser));
    renderHeaderStats();
    renderTeacherQuizAnalytics();
}


/* RANDOM GENERATED PUZZLE ENGINE */
const puzzleBank = [
    {
        title: "यदि एक पौधा 24 घंटे में 100ml जल अवशोषित करता है, तो 5 पौधों को 3 दिन में कितना जल चाहिए?",
        subject: "विज्ञान एवं गणित (Science & Math)",
        options: [
            { text: "1500 ml (1.5 Liters)", correct: true },
            { text: "1000 ml", correct: false },
            { text: "500 ml", correct: false },
            { text: "3000 ml", correct: false }
        ]
    },
    {
        title: "यदि 2x + 10 = 30 हो, तो 5x - 5 का मान क्या होगा?",
        subject: "गणित तार्किक प्रश्न (Math Logic)",
        options: [
            { text: "45 (x = 10 ➔ 50 - 5 = 45)", correct: true },
            { text: "50", correct: false },
            { text: "35", correct: false },
            { text: "25", correct: false }
        ]
    },
    {
        title: "समतल दर्पण के आपतन बिंदु पर आपतित किरण और अभिलंब के बीच 30° का कोण है। परावर्तन कोण कितना होगा?",
        subject: "भौतिक विज्ञान (Physics)",
        options: [
            { text: "30° (आपतन कोण = परावर्तन कोण)", correct: true },
            { text: "60°", correct: false },
            { text: "90°", correct: false },
            { text: "45°", correct: false }
        ]
    },
    {
        title: "प्रकाश संश्लेषण प्रक्रिया में पौधे सूर्य प्रकाश की उपस्थिति में वायुमंडल से कौन सी गैस लेते हैं?",
        subject: "जीव विज्ञान (Biology)",
        options: [
            { text: "कार्बन डाइऑक्साइड (CO2)", correct: true },
            { text: "ऑक्सीजन (O2)", correct: false },
            { text: "नाइट्रोजन (N2)", correct: false },
            { text: "हाइड्रोजन (H2)", correct: false }
        ]
    },
    {
        title: "भारतीय संविधान का प्रारूप किस तिथि को पूर्ण रूप से देश में लागू किया गया था?",
        subject: "सामाजिक विज्ञान (Civics)",
        options: [
            { text: "26 जनवरी 1950", correct: true },
            { text: "15 अगस्त 1947", correct: false },
            { text: "26 नवंबर 1949", correct: false },
            { text: "2 अक्टूबर 1950", correct: false }
        ]
    }
];

function renderPuzzle() {
    const qTitle = document.getElementById('puzzle-q-title');
    const subTag = document.getElementById('puzzle-subject-tag');
    const optionsBox = document.getElementById('puzzle-options');
    const feedback = document.getElementById('puzzle-feedback');

    if (!optionsBox) return;

    const randomIdx = Math.floor(Math.random() * puzzleBank.length);
    const puzzle = puzzleBank[randomIdx];

    if (qTitle) qTitle.innerText = puzzle.title;
    if (subTag) subTag.innerText = puzzle.subject;
    if (feedback) feedback.classList.add('hidden');

    const shuffledOpts = [...puzzle.options].sort(() => Math.random() - 0.5);

    optionsBox.innerHTML = '';
    shuffledOpts.forEach(opt => {
        const btn = document.createElement('button');
        btn.className = 'btn btn-secondary p-opt';
        btn.innerText = opt.text;
        btn.dataset.correct = opt.correct ? 'true' : 'false';
        btn.addEventListener('click', (e) => {
            const isCorrect = e.target.dataset.correct === 'true';
            if (feedback) {
                feedback.classList.remove('hidden');
                if (isCorrect) {
                    feedback.className = 'puzzle-feedback correct';
                    feedback.innerHTML = '🎉 सही उत्तर! (Correct Answer) +20 XP • Streak Protected!';
                    state.currentUser.xp = (state.currentUser.xp || 500) + 20;
                    localStorage.setItem('lq_current_user', JSON.stringify(state.currentUser));
                    renderHeaderStats();
                } else {
                    feedback.className = 'puzzle-feedback incorrect';
                    feedback.innerHTML = '❌ गलत उत्तर! पुनः प्रयास करें या दूसरा पहेली प्रश्न जनरेट करें!';
                }
            }
        });
        optionsBox.appendChild(btn);
    });
}

/* SCHOLARSHIPS & CAREERS */
function renderScholarshipsAndCareers() {
    const sGrid = document.getElementById('scholarships-grid');
    if (sGrid) {
        sGrid.innerHTML = '';
        state.scholarships.forEach(s => {
            const card = document.createElement('div');
            card.className = 'card';
            card.innerHTML = `
                <div class="opp-card-title">${s.title}</div>
                <div class="opp-card-provider">Provider: ${s.provider}</div>
                <div class="opp-tags">
                    <span class="tag verified">✓ Verified (${s.lastVerifiedDate || 'Recent'})</span>
                    <span class="tag">${s.category}</span>
                    <span class="tag" style="background: var(--forest-green-light); color: var(--forest-green); font-weight: 700;">${s.amount}</span>
                </div>
                <p style="font-size: 13px; color: var(--text-secondary); margin-bottom: 6px;"><strong>Eligibility:</strong> ${s.eligibility}</p>
                <p style="font-size: 12px; color: var(--weak-red); font-weight: 600;"><strong>Deadline:</strong> ${s.deadline}</p>
            `;
            sGrid.appendChild(card);
        });
    }

    const cGrid = document.getElementById('careers-grid');
    if (cGrid) {
        cGrid.innerHTML = '';
        state.careers.forEach(c => {
            const card = document.createElement('div');
            card.className = 'card';
            card.innerHTML = `
                <div class="opp-card-title">${c.title}</div>
                <div class="opp-card-provider">Sector: ${c.sector} • ${c.requiredEducation}</div>
                <p style="font-size: 13px; margin-bottom: 12px;">${c.description}</p>
                <div class="opp-tags">
                    ${(c.recommendedSubjects || []).map(sub => `<span class="tag">${sub}</span>`).join('')}
                </div>
            `;
            cGrid.appendChild(card);
        });
    }
}
