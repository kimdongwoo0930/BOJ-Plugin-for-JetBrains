package com.github.kimdongwoo0930.bojpluginforjetbrains.toolWindow

import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.components.JBPanel
import com.intellij.ui.content.ContentFactory
import java.awt.GridLayout
import javax.swing.JButton
import javax.swing.border.EmptyBorder

class MyToolWindowFactory : ToolWindowFactory {

    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        val panel = BOJToolWindow(project).getContent()
        val content = ContentFactory.getInstance().createContent(panel, null, false)
        toolWindow.contentManager.addContent(content)
    }

    override fun shouldBeAvailable(project: Project) = true
}

class BOJToolWindow(private val project: Project) {

    fun getContent() = JBPanel<JBPanel<*>>().apply {
        layout = GridLayout(5, 1, 0, 8)
        border = EmptyBorder(16, 16, 16, 16)

        add(JButton("📝 새 문제 시작하기").apply {
            cursor = java.awt.Cursor(java.awt.Cursor.HAND_CURSOR)
            addActionListener { /* TODO: 문제 번호 입력 */ }
        })
        add(JButton("👁️ 문제만 먼저 보기").apply {
            cursor = java.awt.Cursor(java.awt.Cursor.HAND_CURSOR)
            addActionListener { /* TODO: 파일 없이 문제 보기 */ }
        })
        add(JButton("🧪 예제로 채점하기").apply {
            cursor = java.awt.Cursor(java.awt.Cursor.HAND_CURSOR)
            addActionListener { /* TODO: 테스트 케이스 실행 */ }
        })
        add(JButton("📂 문제 다시 열기").apply {
            cursor = java.awt.Cursor(java.awt.Cursor.HAND_CURSOR)
            addActionListener { /* TODO: 문제 다시 보기 */ }
        })
        add(JButton("🚀 제출하기").apply {
            cursor = java.awt.Cursor(java.awt.Cursor.HAND_CURSOR)
            addActionListener { /* TODO: 제출 */ }
        })
    }
}