package com.github.kimdongwoo0930.bojpluginforjetbrains.toolWindow

import com.intellij.openapi.fileEditor.FileEditor
import com.intellij.openapi.fileEditor.FileEditorPolicy
import com.intellij.openapi.fileEditor.FileEditorProvider
import com.intellij.openapi.fileEditor.FileEditorState
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Disposer
import com.intellij.openapi.util.UserDataHolderBase
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.ui.jcef.JBCefBrowser
import java.awt.BorderLayout
import java.beans.PropertyChangeListener
import javax.swing.JComponent
import javax.swing.JPanel

/**
 * 백준 문제를 HTML로 렌더링하는 에디터
 * VSCode의 Webview와 동일한 역할
 */
class ProblemViewer(private val file: VirtualFile) : UserDataHolderBase(),FileEditor {

    private val browser = JBCefBrowser()
    private val panel = JPanel(BorderLayout()).apply {
        add(browser.component, BorderLayout.CENTER)
    }

    init {
        val html = file.contentsToByteArray().toString(Charsets.UTF_8)
        browser.loadHTML(html)
    }

    override fun getComponent(): JComponent = panel
    override fun getPreferredFocusedComponent(): JComponent = panel
    override fun getName(): String = "문제 보기"
    override fun setState(state: FileEditorState) {}
    override fun isModified(): Boolean = false
    override fun isValid(): Boolean = true
    override fun addPropertyChangeListener(listener: PropertyChangeListener) {}
    override fun removePropertyChangeListener(listener: PropertyChangeListener) {}
    override fun getFile(): VirtualFile = file
    override fun dispose() { Disposer.dispose(browser) }
}

/**
 * .html 파일을 ProblemViewerEditor로 여는 프로바이더
 */
class ProblemViewerProvider : FileEditorProvider, DumbAware {

    override fun accept(project: Project, file: VirtualFile): Boolean {
        // BOJ 문제 HTML 파일만 처리 (숫자.html 형식)
        return file.extension == "html" && file.nameWithoutExtension.all { it.isDigit() }
    }

    override fun createEditor(project: Project, file: VirtualFile): FileEditor {
        return ProblemViewer(file)
    }

    override fun getEditorTypeId(): String = "BOJ-Problem-Viewer"
    override fun getPolicy(): FileEditorPolicy = FileEditorPolicy.HIDE_DEFAULT_EDITOR
}