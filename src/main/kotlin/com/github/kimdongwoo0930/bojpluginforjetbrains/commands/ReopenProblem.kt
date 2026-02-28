package com.github.kimdongwoo0930.bojpluginforjetbrains.commands

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.fileEditor.ex.FileEditorManagerEx
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages

/**
 * 현재 열려있는 파일 기준으로 문제를 다시 여는 함수
 * VSCode의 reopenProblem 함수와 동일한 역할
 * @param project 현재 IntelliJ 프로젝트
 */

fun reopenProblem(project: Project) {
    val editorManager = FileEditorManager.getInstance(project)

    // 현재 열려있는 파일 가져오기
    val currentFile = editorManager.selectedFiles.firstOrNull()

    if (currentFile == null) {
        Messages.showErrorDialog(project, "열려있는 파일이 없습니다.", "오류")
        return
    }

    // 현재 파일의 상위 폴더 (문제 폴더)
    val problemFolder = currentFile.parent

    if (problemFolder == null) {
        Messages.showErrorDialog(project, "문제 폴더를 찾을 수 없습니다.", "오류")
        return
    }

    // 폴더명에서 문제 번호 추출 ex) "1000번 - A+B" → "1000"
    val problemNumber = problemFolder.name
        .substringBefore("번 - ")
        .trim()

    if (problemNumber.isBlank() || !problemNumber.all { it.isDigit() }) {
        Messages.showErrorDialog(project, "현재 파일이 BOJ 문제 폴더 안에 있지 않습니다.", "오류")
        return
    }

    // HTML 파일과 코드 파일 찾기
    val htmlFile = problemFolder.findChild("$problemNumber.html")
    val codeFile = problemFolder.children.firstOrNull {
        it.name == "Main.java" || it.name == "app.py"
    }

    if (htmlFile == null || codeFile == null) {
        Messages.showErrorDialog(project, "문제 파일을 찾을 수 없습니다.", "오류")
        return
    }

    // 분할 창으로 열기
    ApplicationManager.getApplication().invokeLater {
        val exEditorManager = FileEditorManagerEx.getInstanceEx(project)

        if (codeFile != null) {
            exEditorManager.openFile(codeFile, true)
        }
        if (htmlFile != null) {
            htmlFile.refresh(false, false)
            val currentWindow = exEditorManager.currentWindow
            currentWindow?.split(javax.swing.SwingConstants.VERTICAL, true, htmlFile, true)
        }
    }
}