package com.github.kimdongwoo0930.bojpluginforjetbrains.commands

import com.github.kimdongwoo0930.bojpluginforjetbrains.utils.getProblemData
import com.github.kimdongwoo0930.bojpluginforjetbrains.utils.makeFolder
import com.intellij.openapi.application.ApplicationInfo
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.fileChooser.FileChooser
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.progress.Task
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages

/**
 * 문제 번호를 입력받아 백준 문제를 가져오는 함수
 * VSCode의 InputProblemNumber 함수와 동일한 역할
 * @param project 현재 IntelliJ 프로젝트
 */
fun getProblemByNumber(project: Project) {
    val problemNumber = Messages.showInputDialog(
        project,
        "문제 번호를 입력해 주세요.",
        "새 문제 시작하기",
        Messages.getQuestionIcon(),
        "",
        null
    ) ?: return

    if (problemNumber.isBlank() || !problemNumber.all { it.isDigit() }) {
        Messages.showErrorDialog(project, "올바른 번호를 입력해주세요.", "오류")
        getProblemByNumber(project)
        return
    }

    val (langName, langExt) = detectLanguage()

    val descriptor = FileChooserDescriptorFactory.createSingleFolderDescriptor()
    descriptor.title = "문제 파일을 저장할 폴더를 선택해주세요."
    val folder = FileChooser.chooseFile(descriptor, project, null) ?: return


// 이미 존재하는 문제인지 체크
    val alreadyExists = folder.toNioPath().toFile().listFiles()
        ?.any { it.isDirectory && it.name.startsWith("${problemNumber}번 - ") }
        ?: false

    if (alreadyExists) {
        Messages.showInfoMessage(project, "${problemNumber}번 문제는 이미 존재합니다.", "알림")
        return
    }

    // 백그라운드에서 스크래핑 + 파일 생성
    ProgressManager.getInstance().run(object : Task.Backgroundable(project, "문제 불러오는 중...") {
        override fun run(indicator: com.intellij.openapi.progress.ProgressIndicator) {
            val problemData = getProblemData(problemNumber)

            if (problemData == null) {
                ApplicationManager.getApplication().invokeLater {
                    Messages.showErrorDialog(project, "존재하지 않는 문제입니다.", "오류")
                }
                return
            }

            makeFolder(folder.path, problemNumber, langExt, problemData, project)
        }
    })
}



/**
 * 현재 실행 중인 IDE에 따라 언어를 자동 선택하는 함수
 * IntelliJ → Java, PyCharm → Python
 * @return Pair(언어 이름, 확장자)
 */
fun detectLanguage(): Pair<String, String> {
    val ideName = ApplicationInfo.getInstance().fullApplicationName
    return if (ideName.contains("PyCharm")) {
        Pair("Python", "py")
    } else {
        Pair("Java", "java")
    }
}