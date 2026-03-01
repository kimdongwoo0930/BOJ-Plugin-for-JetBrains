package com.github.kimdongwoo0930.bojpluginforjetbrains.commands

import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.ide.CopyPasteManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages
import java.awt.Desktop
import java.awt.datatransfer.StringSelection
import java.net.URI

/**
 * 현재 열린 파일의 코드를 클립보드에 복사하고 백준 제출 페이지를 여는 함수
 * VSCode의 submitAnswer 함수와 동일한 역할
 * @param project 현재 IntelliJ 프로젝트
 */
fun submitAnswer(project: Project) {
    val editorManager = FileEditorManager.getInstance(project)
    val currentFile = editorManager.selectedFiles.firstOrNull {
        it.extension == "java" || it.extension == "py"
    }

    if (currentFile == null) {
        Messages.showErrorDialog(project, "열려있는 파일이 없습니다.", "오류")
        return
    }

    // 폴더명에서 문제 번호 추출 ex) "1000번 - A+B" → "1000"
    val problemFolder = currentFile.parent
    val problemNumber = problemFolder?.name
        ?.substringBefore("번 - ")
        ?.trim()

    if (problemNumber.isNullOrBlank() || !problemNumber.all { it.isDigit() }) {
        Messages.showErrorDialog(project, "문제 번호를 찾을 수 없습니다.", "오류")
        return
    }

    // 코드 읽기
    val code = currentFile.contentsToByteArray().toString(Charsets.UTF_8)
    val ext = currentFile.extension ?: ""

    // 주석 제거
    val cleaned = when (ext) {
        "py" -> code
            .replace(Regex("#.*$", RegexOption.MULTILINE), "")
            .replace(Regex("^\\s*\\n", RegexOption.MULTILINE), "")
        else -> code
            .replace(Regex("//.*$", RegexOption.MULTILINE), "")
            .replace(Regex("/\\*[\\s\\S]*?\\*/"), "")
            .replace(Regex("^\\s*\\n", RegexOption.MULTILINE), "")
    }

    // 클립보드에 복사
    CopyPasteManager.getInstance().setContents(StringSelection(cleaned))

    // 백준 제출 페이지 열기
    val submitUrl = "https://www.acmicpc.net/submit/$problemNumber"
    Desktop.getDesktop().browse(URI(submitUrl))

    Messages.showInfoMessage(
        project,
        "${problemNumber}번 코드가 복사되었습니다. 제출 페이지에서 붙여넣기 해주세요.",
        "제출하기"
    )
}