import re
from pathlib import Path

APP_JS_PATH = Path("web/app.js")

def test_no_double_publish_quiz_event_bindings():
    """
    Regression Test 1:
    handlePublishQuizSubmit must NOT be bound to BOTH createQuizForm submit AND publishQuizBtn click simultaneously,
    as having both causes handlePublishQuizSubmit() to run twice in a row, resetting the form on the first run
    and causing validation errors or duplicate execution on the second run.
    """
    content = APP_JS_PATH.read_text(encoding="utf-8")
    
    has_submit_binding = "createQuizForm.addEventListener" in content and "'submit'" in content
    has_click_binding = "publishQuizBtn.addEventListener" in content and "'click'" in content
    
    assert not (has_submit_binding and has_click_binding), (
        "handlePublishQuizSubmit is bound to BOTH createQuizForm submit AND publishQuizBtn click! "
        "Remove one of the bindings so handlePublishQuizSubmit executes exactly once per submission."
    )

def test_get_combined_quiz_bank_filters_by_grade():
    """
    Regression Test 2:
    getCombinedQuizBank must filter custom quiz sets by grade so that a quiz published for
    a specific class appears in that class's adaptive quiz engine.
    """
    content = APP_JS_PATH.read_text(encoding="utf-8")
    
    # Extract getCombinedQuizBank function implementation
    match = re.search(r"function getCombinedQuizBank\s*\([^)]*\)\s*\{([\s\S]*?)\n\}", content)
    assert match is not None, "getCombinedQuizBank function not found in web/app.js"
    
    fn_body = match.group(1)
    
    # Must check grade filtering
    assert "set.grade" in fn_body or "targetGrade" in fn_body, (
        "getCombinedQuizBank does not filter custom quiz questions by grade! "
        "Ensure custom quiz questions are matched against student's grade."
    )

def test_no_duplicate_quiz_question_mutation():
    """
    Regression Test 3:
    handlePublishQuizSubmit should NOT mutate state.subjectQuizBank[subject] directly while also
    saving to localStorage via saveCustomQuizSetToStorage, as that causes questions to duplicate.
    """
    content = APP_JS_PATH.read_text(encoding="utf-8")
    
    match = re.search(r"function handlePublishQuizSubmit\s*\([^)]*\)\s*\{([\s\S]*?)\n\}", content)
    assert match is not None, "handlePublishQuizSubmit function not found in web/app.js"
    
    fn_body = match.group(1)
    
    assert "state.subjectQuizBank[subject].push" not in fn_body, (
        "handlePublishQuizSubmit mutates state.subjectQuizBank[subject] directly, causing duplicate questions "
        "when combined with getStoredQuizQuestions() in getCombinedQuizBank."
    )

def test_grade_specific_quiz_upload():
    """
    Regression Test 4:
    Verify that custom quiz set objects have 'grade', 'subject', and 'questions' properties,
    and getCombinedQuizBank logic handles targetGrade matching.
    """
    content = APP_JS_PATH.read_text(encoding="utf-8")
    
    # Verify saveCustomQuizSetToStorage call in handlePublishQuizSubmit
    match = re.search(r"saveCustomQuizSetToStorage\s*\(\s*\{\s*grade,\s*subject,\s*questions:\s*questionsList\s*\}\s*\)", content)
    assert match is not None, "handlePublishQuizSubmit must call saveCustomQuizSetToStorage with grade, subject, and questions"

def test_rich_scholarships_and_careers_dataset():
    """
    Regression Test 5:
    Ensure state.scholarships and state.careers contain multiple rich entries for student exploration.
    """
    content = APP_JS_PATH.read_text(encoding="utf-8")
    
    s_count = len(re.findall(r"provider:\s*['\"]", content))
    assert s_count >= 3, f"Expected at least 3 scholarships in dataset, found {s_count}"
    
    c_count = len(re.findall(r"sector:\s*['\"]", content))
    assert c_count >= 3, f"Expected at least 3 career pathways in dataset, found {c_count}"
