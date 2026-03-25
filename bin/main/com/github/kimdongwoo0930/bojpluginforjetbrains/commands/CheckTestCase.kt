package com.github.kimdongwoo0930.bojpluginforjetbrains.commands

import com.github.kimdongwoo0930.bojpluginforjetbrains.utils.getProblemData
import com.intellij.execution.ui.ConsoleViewContentType
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.progress.Task
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.wm.ToolWindowManager
import com.intellij.openapi.roots.ProjectRootManager
import com.intellij.execution.filters.TextConsoleBuilderFactory
import java.io.File

/**
 * 현재 열린 파일의 테스트 케이스를 실행하는 함수
 * VSCode의 checkTestCase 함수와 동일한 역할
 * @param project 현재 IntelliJ 프로젝트
 */
fun checkTestCase(project: Project) {
    val editorManager = FileEditorManager.getInstance(project)
    val currentFile = editorManager.selectedFiles.firstOrNull()

    if (currentFile == null) {
        Messages.showErrorDialog(project, "열려있는 파일이 없습니다.", "오류")
        return
    }

    val lang = currentFile.extension ?: ""
    if (lang != "java" && lang != "py") {
        Messages.showErrorDialog(project, "테스트할 파일을 열고 실행해 주세요. (java, py만 지원)", "오류")
        return
    }

    // 같은 폴더에서 HTML 파일 찾기 → 문제 번호 추출
    val problemFolder = currentFile.parent
    val htmlFile = problemFolder?.children?.firstOrNull { it.extension == "html" }

    if (htmlFile == null) {
        Messages.showErrorDialog(project, "HTML 파일을 찾을 수 없습니다. BOJ 문제 폴더인지 확인해주세요.", "오류")
        return
    }

    val problemNumber = htmlFile.nameWithoutExtension

    val filePath = currentFile.path

    // 콘솔 생성
    val consoleView = TextConsoleBuilderFactory.getInstance()
        .createBuilder(project)
        .console

    // Run 툴윈도우에 콘솔 표시
    ApplicationManager.getApplication().invokeLater {
        val toolWindow = ToolWindowManager.getInstance(project).getToolWindow("BOJ")
        val contentManager = toolWindow?.contentManager

        // 기존 테스트 결과 탭이 있으면 제거
        val existing = contentManager?.contents?.firstOrNull {
            it.displayName == "테스트 결과"
        }
        if (existing != null) {
            contentManager?.removeContent(existing, true)
        }

        val content = com.intellij.ui.content.ContentFactory.getInstance()
            .createContent(consoleView.component, "테스트 결과", false)

        contentManager?.addContent(content)
        contentManager?.setSelectedContent(content)
        toolWindow?.show()
    }

    // 백그라운드에서 테스트 실행
    ProgressManager.getInstance().run(object : Task.Backgroundable(project, "테스트 실행 중...") {
        override fun run(indicator: ProgressIndicator) {
            val problemData = getProblemData(problemNumber)

            if (problemData == null) {
                ApplicationManager.getApplication().invokeLater {
                    Messages.showErrorDialog(project, "문제 데이터를 가져오는데 실패했습니다.", "오류")
                }
                return
            }

            val title = "═".repeat(50) + "\n" +
                    " ${problemNumber}번: ${problemData.title}\n" +
                    "═".repeat(50) + "\n"

            consoleView.print(title, ConsoleViewContentType.SYSTEM_OUTPUT)

            var passCount = 0
            val totalCount = problemData.testCaseInputs.size

            for (i in problemData.testCaseInputs.indices) {
                indicator.text = "테스트 케이스 ${i + 1} / $totalCount 실행 중..."

                val result = runTestCase(
                    lang = lang,
                    filePath = filePath,
                    input = problemData.testCaseInputs[i],
                    expectedOutput = problemData.testCaseOutputs[i],
                    index = i,
                    consoleView = consoleView,
                    project = project
                )
                if (result) passCount++
            }

            // 최종 결과 출력
            val summary = "\n" + "─".repeat(50) + "\n" +
                    "📊 RESULT SUMMARY\n" +
                    "✔ $passCount / $totalCount Tests Passed " +
                    "(${"%.1f".format(passCount.toDouble() / totalCount * 100)}%)\n" +
                    "─".repeat(50) + "\n"

            val contentType = if (passCount == totalCount)
                ConsoleViewContentType.NORMAL_OUTPUT
            else
                ConsoleViewContentType.ERROR_OUTPUT

            consoleView.print(summary, contentType)
        }
    })
}

/**
 * 단일 테스트 케이스를 실행하는 함수
 */
private fun runTestCase(
    lang: String,
    filePath: String,
    input: String,
    expectedOutput: String,
    index: Int,
    consoleView: com.intellij.execution.ui.ConsoleView,
    project : Project
): Boolean {
    val cwd = File(filePath).parent
    val isWin = System.getProperty("os.name").lowercase().contains("win")

//    consoleView.print("DEBUG input: [${input.replace("\n", "\\n")}]\n", ConsoleViewContentType.SYSTEM_OUTPUT)
//    consoleView.print("DEBUG input: [${input}]", ConsoleViewContentType.SYSTEM_OUTPUT)


    return try {
        val startTime = System.currentTimeMillis()

        val process = when (lang) {
            "java" -> {
                val fileName = File(filePath).name
                val className = File(filePath).nameWithoutExtension

                // 컴파일
                val compileProcess = ProcessBuilder("javac", "-encoding", "UTF-8", "-cp", ".", fileName)
                    .directory(File(cwd))
                    .redirectErrorStream(true)
                    .start()

                val compileError = compileProcess.inputStream.bufferedReader().readText()
                compileProcess.waitFor()

                if (compileProcess.exitValue() != 0) {
                    consoleView.print(
                        "═".repeat(50) + "\n" +
                                "[ Test Case ${index + 1} ]  ⚠️ COMPILE ERROR\n" +
                                "─".repeat(50) + "\n" +
                                compileError + "\n" +
                                "═".repeat(50) + "\n",
                        ConsoleViewContentType.ERROR_OUTPUT
                    )
                    return false
                }

                // 실행
                ProcessBuilder("java", "-cp", ".", className)
                    .directory(File(cwd))
                    .redirectErrorStream(false)
                    .start()
            }

            "py" -> {
                val pythonPath = getPythonPath(project)
                ProcessBuilder(pythonPath, filePath)
                    .directory(File(cwd))
                    .redirectErrorStream(false)
                    .start()
            }

            else -> return false
        }

        // 입력 전달
        process.outputStream.write(input.toByteArray(Charsets.UTF_8))
        process.outputStream.flush()
        process.outputStream.close()



        val actual = process.inputStream.bufferedReader(Charsets.UTF_8).readText().trim()
        val error = process.errorStream.bufferedReader(Charsets.UTF_8).readText().trim()

        process.waitFor()
        val endTime = System.currentTimeMillis()
        val elapsed = endTime - startTime

        val expected = expectedOutput.trim()
        val actualNorm = actual.replace("\r\n", "\n").replace("\r", "\n").trim()
        val expectedNorm = expected.replace("\r\n", "\n").replace("\r", "\n").trim()
        val isPass = process.exitValue() == 0 && actualNorm == expectedNorm

        if (error.isNotEmpty()) {
            consoleView.print(
                "═".repeat(50) + "\n" +
                        "[ Test Case ${index + 1} ]  ⚠️ RUNTIME ERROR\n" +
                        "─".repeat(50) + "\n" +
                        error + "\n" +
                        "═".repeat(50) + "\n",
                ConsoleViewContentType.ERROR_OUTPUT
            )
            return false
        }

        val resultText = "═".repeat(50) + "\n" +
                "[ Test Case ${index + 1} ]  ${if (isPass) "✅ PASS" else "❌ FAIL"}\n" +
                "─".repeat(50) + "\n" +
                "입력       : ${input.trim().replace("\n", " ")}\n" +
                "예상 출력  : ${expectedNorm.replace("\n", " ")}\n" +
                "실제 출력  : ${actualNorm.replace("\n", " ")}\n" +
                "─".repeat(50) + "\n" +
                "실행 시간  : ${elapsed} ms\n" +
                "═".repeat(50) + "\n"

        consoleView.print(
            resultText,
            if (isPass) ConsoleViewContentType.NORMAL_OUTPUT else ConsoleViewContentType.ERROR_OUTPUT
        )

        isPass

    } catch (e: Exception) {
        consoleView.print(
            "═".repeat(50) + "\n" +
                    "[ Test Case ${index + 1} ]  ⚠️ ERROR: ${e.message}\n" +
                    "═".repeat(50) + "\n",
            ConsoleViewContentType.ERROR_OUTPUT
        )
        false
    }
}


fun getPythonPath(project: Project): String {
    val sdkPath = ProjectRootManager.getInstance(project).projectSdk?.homePath
    if (sdkPath != null) return sdkPath 

    val candidates = if (System.getProperty("os.name").lowercase().contains("win")) {
        listOf("python", "py", "python3")
    } else {
        listOf("python3", "python")
    }

    for (cmd in candidates) {
        try {
            val result = ProcessBuilder(cmd, "--version")
                .start()
                .waitFor()
            if (result == 0) return cmd
        } catch (e: Exception) {
            continue
        }
    }

    return "python"
}
