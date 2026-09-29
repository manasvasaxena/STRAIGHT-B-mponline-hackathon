import re
from pathlib import Path

APP_JS_PATH = Path("web/app.js")
INDEX_HTML_PATH = Path("web/index.html")

def test_module_timestamps_present():
    """
    Test 1:
    Verify that modules contain timestamp metadata and render levels displays timestamp badges.
    """
    app_js = APP_JS_PATH.read_text(encoding="utf-8")
    assert "timestamp:" in app_js or "Date.now()" in app_js, "Modules must include timestamp metadata"
    assert "formatTimestamp" in app_js or "🕒" in app_js, "renderLevels must display module timestamps"

def test_random_puzzles_generator():
    """
    Test 2:
    Verify that random puzzles generator is implemented for student streak protection and brain teasers.
    """
    app_js = APP_JS_PATH.read_text(encoding="utf-8")
    assert "puzzleBank" in app_js, "Puzzles engine must feature a puzzle bank"
    assert "generate-new-puzzle-btn" in app_js, "Must support random puzzle generation button"

def test_assignment_studio_and_tab():
    """
    Test 3:
    Verify that student assignment tab, teacher assignment studio, and assignment deletion are implemented.
    """
    index_html = INDEX_HTML_PATH.read_text(encoding="utf-8")
    app_js = APP_JS_PATH.read_text(encoding="utf-8")
    
    assert "tab-assignments" in index_html, "Student assignments tab must exist in index.html"
    assert "teacher-sec-create-assignment" in index_html, "Teacher assignment creation section must exist in index.html"
    assert "renderStudentAssignments" in app_js, "renderStudentAssignments function must be implemented in app.js"
    assert "deleteAssignmentFromStorage" in app_js, "Teacher assignment deletion must be implemented in app.js"

def test_student_assignment_filters():
    """
    Test 4:
    Verify that student assignment tab includes Class/Grade and Subject filters
    so students can view assignments published for their grade and subject.
    """
    index_html = INDEX_HTML_PATH.read_text(encoding="utf-8")
    app_js = APP_JS_PATH.read_text(encoding="utf-8")
    
    assert "assign-grade-select" in index_html, "Student assignments tab must feature assign-grade-select filter"
    assert "assign-subject-select" in index_html, "Student assignments tab must feature assign-subject-select filter"
    assert "selectedAssignmentGrade" in app_js or "selectedAssignmentSubject" in app_js, "app.js must manage student assignment filters"

def test_adaptive_quiz_removed():
    """
    Test 5:
    Verify that Adaptive Quiz tab has been removed per user request.
    """
    index_html = INDEX_HTML_PATH.read_text(encoding="utf-8")
    assert "tab-quiz" not in index_html, "Adaptive quiz section #tab-quiz must be removed from index.html"
