package com.github.kimdongwoo0930.bojpluginforjetbrains.commands

import com.github.kimdongwoo0930.bojpluginforjetbrains.utils.buildHtml
import com.github.kimdongwoo0930.bojpluginforjetbrains.utils.getProblemData
import com.intellij.ide.highlighter.HtmlFileType
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.progress.Task
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages
import com.intellij.testFramework.LightVirtualFile

/**
 * 파일/폴더 생성 없이 문제 내용만 미리보기
 * @param project 현재 IntelliJ 프로젝트
 */
fun previewProblem(project: Project) {
    val problemNumber = Messages.showInputDialog(
        project,
        "문제 번호를 입력해 주세요.",
        "빠른 미리보기",
        Messages.getQuestionIcon(),
        "",
        null
    ) ?: return

    if (problemNumber.isBlank() || !problemNumber.all { it.isDigit() }) {
        Messages.showErrorDialog(project, "올바른 번호를 입력해주세요.", "오류")
        previewProblem(project)
        return
    }

    ProgressManager.getInstance().run(object : Task.Backgroundable(project, "문제 불러오는 중...") {
        override fun run(indicator: com.intellij.openapi.progress.ProgressIndicator) {
            val problemData = getProblemData(problemNumber)

            if (problemData == null) {
                ApplicationManager.getApplication().invokeLater {
                    Messages.showErrorDialog(project, "존재하지 않는 문제입니다.", "오류")
                }
                return
            }

            val html = buildHtml(problemData)
            val virtualFile = LightVirtualFile("$problemNumber.html", HtmlFileType.INSTANCE, html)

            ApplicationManager.getApplication().invokeLater {
                FileEditorManager.getInstance(project).openFile(virtualFile, true)
            }
        }
    })
}
