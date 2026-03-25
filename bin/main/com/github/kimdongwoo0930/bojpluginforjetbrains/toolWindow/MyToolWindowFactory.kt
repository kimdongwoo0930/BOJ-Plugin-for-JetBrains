package com.github.kimdongwoo0930.bojpluginforjetbrains.toolWindow

import com.github.kimdongwoo0930.bojpluginforjetbrains.commands.checkTestCase
import com.github.kimdongwoo0930.bojpluginforjetbrains.commands.getProblemByNumber
import com.github.kimdongwoo0930.bojpluginforjetbrains.commands.previewProblem
import com.github.kimdongwoo0930.bojpluginforjetbrains.commands.reopenProblem
import com.github.kimdongwoo0930.bojpluginforjetbrains.commands.submitAnswer
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.JBColor.border
import com.intellij.ui.components.JBPanel
import com.intellij.ui.content.ContentFactory
import java.awt.GridLayout
import javax.swing.Box
import javax.swing.BoxLayout
import javax.swing.JButton
import javax.swing.JLabel
import javax.swing.JSeparator
import javax.swing.border.EmptyBorder

class MyToolWindowFactory : ToolWindowFactory {

    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        val panel = BOJToolWindow(project).getContent()
        val content = ContentFactory.getInstance().createContent(panel, "메뉴", false)
        toolWindow.contentManager.addContent(content)
    }

    override fun shouldBeAvailable(project: Project) = true
}

class BOJToolWindow(private val project: Project) {

    /**
     * BOJ 플러그인 사이드바 UI를 생성합니다.
     * @return JBPanel 사이드바 패널
     */

    fun getContent() = JBPanel<JBPanel<*>>().apply {
        layout = BoxLayout(this, BoxLayout.Y_AXIS)
        border = EmptyBorder(16, 16, 16, 16)

        // 문제 생성 섹션
        add(sectionLabel("📄 문제 생성"))
        add(actionButton("+ 새 문제 시작하기") {
            getProblemByNumber(project)
        })
        add(descLabel("문제 번호만 입력하면 폴더 + 파일 자동 생성"))
        add(Box.createVerticalStrut(24))
        add(divider())

        // 빠른 미리보기 섹션
        add(sectionLabel("👁️ 빠른 미리보기"))
        add(actionButton("문제만 먼저 보기") { previewProblem(project) })
        add(descLabel("파일 생성 없이 문제 내용만 확인"))
        add(Box.createVerticalStrut(24))
        add(divider())


        // 코드 테스트 섹션
        add(sectionLabel("🧪 코드 테스트"))
        add(actionButton("예제로 채점하기") { checkTestCase(project) })
        add(descLabel("작성한 코드를 백준 예제로 자동 테스트"))
        add(Box.createVerticalStrut(24))
        add(divider())


        // 작업 재개 섹션
        add(sectionLabel("📂 작업 재개"))
        add(actionButton("문제 다시 열기") { reopenProblem(project) })
        add(descLabel("현재 작업 중인 문제 빠르게 확인"))
        add(Box.createVerticalStrut(24))
        add(divider())


        // 제출 섹션
        add(sectionLabel("🚀 제출"))
        add(actionButton("제출하기") { submitAnswer(project) })
        add(descLabel("코드를 백준에 제출"))
        add(Box.createVerticalStrut(24))
        add(divider())

    }

    private fun sectionLabel(text: String) = JLabel(text).apply {
        font = font.deriveFont(java.awt.Font.BOLD, 13f)
        alignmentX = java.awt.Component.LEFT_ALIGNMENT
        border = EmptyBorder(0, 0, 6, 0)
    }

    private fun descLabel(text: String) = JLabel(text).apply {
        font = font.deriveFont(11f)
        foreground = java.awt.Color(150, 150, 150)
        alignmentX = java.awt.Component.LEFT_ALIGNMENT
        border = EmptyBorder(4, 0, 0, 0)
    }

    private fun divider() = JSeparator().apply {
        alignmentX = java.awt.Component.LEFT_ALIGNMENT
        maximumSize = java.awt.Dimension(Int.MAX_VALUE, 1)
        border = EmptyBorder(8, 0, 8, 0)
    }

    private fun actionButton(text: String, action: () -> Unit) = JButton(text).apply {
        cursor = java.awt.Cursor(java.awt.Cursor.HAND_CURSOR)
        isFocusable = false
        alignmentX = java.awt.Component.LEFT_ALIGNMENT
        maximumSize = java.awt.Dimension(Int.MAX_VALUE, 36)
        background = java.awt.Color(60, 63, 65)
        foreground = java.awt.Color.WHITE
        isOpaque = true
        isBorderPainted = false

        addMouseListener(object : java.awt.event.MouseAdapter() {
            override fun mouseEntered(e: java.awt.event.MouseEvent) {
                background = java.awt.Color(75, 110, 175) // 호버 시 파란색
            }
            override fun mouseExited(e: java.awt.event.MouseEvent) {
                background = java.awt.Color(60, 63, 65) // 기본 색상
            }
        })

        addActionListener { action() }
    }

}