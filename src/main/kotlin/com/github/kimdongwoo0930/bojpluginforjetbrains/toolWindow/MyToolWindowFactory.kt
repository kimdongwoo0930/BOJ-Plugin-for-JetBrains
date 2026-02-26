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

    /**
     * BOJ 플러그인 사이드바 UI를 생성합니다.
     * @return JBPanel 사이드바 패널
     */

    fun getContent() = JBPanel<JBPanel<*>>().apply {
        layout = GridLayout(5, 1, 0, 8)
        border = EmptyBorder(16, 16, 16, 16)

        // 새 문제 시작하기 버튼 - 문제 번호 입력 후 폴더/파일 생성
        add(JButton("📝 새 문제 시작하기").apply {
            cursor = java.awt.Cursor(java.awt.Cursor.HAND_CURSOR)
            addActionListener { /* TODO: 문제 번호 입력 */ }
        })

        // 문제만 먼저 보기 버튼 - 파일 생성 없이 문제 내용만 확인
        add(JButton("👁️ 문제만 먼저 보기").apply {
            cursor = java.awt.Cursor(java.awt.Cursor.HAND_CURSOR)
            addActionListener { /* TODO: 파일 없이 문제 보기 */ }
        })

        // 예제로 채점하기 버튼 - 작성한 코드를 예제 입출력으로 테스트
        add(JButton("🧪 예제로 채점하기").apply {
            cursor = java.awt.Cursor(java.awt.Cursor.HAND_CURSOR)
            addActionListener { /* TODO: 테스트 케이스 실행 */ }
        })

        // 문제 다시 열기 버튼 - 현재 작업 중인 문제 다시 보기
        add(JButton("📂 문제 다시 열기").apply {
            cursor = java.awt.Cursor(java.awt.Cursor.HAND_CURSOR)
            addActionListener { /* TODO: 문제 다시 보기 */ }
        })

        // 제출하기 버튼 - 백준에 코드 제출
        add(JButton("🚀 제출하기").apply {
            cursor = java.awt.Cursor(java.awt.Cursor.HAND_CURSOR)
            addActionListener { /* TODO: 제출 */ }
        })
    }
}