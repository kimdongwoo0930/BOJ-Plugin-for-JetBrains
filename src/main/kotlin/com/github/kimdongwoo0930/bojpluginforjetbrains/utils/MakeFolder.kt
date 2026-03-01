package com.github.kimdongwoo0930.bojpluginforjetbrains.utils

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.LocalFileSystem
import java.io.File

/**
 * 문제 폴더와 파일을 생성하는 함수
 * VSCode의 makeFolder 함수와 동일한 역할
 * @param folderPath 저장할 폴더 경로
 * @param problemNumber 문제 번호
 * @param langExt 언어 확장자 (java, py)
 * @param problemData 문제 데이터
 * @param project 현재 IntelliJ 프로젝트
 */
fun makeFolder(
    folderPath: String,
    problemNumber: String,
    langExt: String,
    problemData: ProblemData,
    project: Project
) {
    // 문제 제목에서 특수문자 제거 (Windows 파일명 규칙)
    val safeTitle = problemData.title
        .replace("[\\\\/:*?\"<>|]".toRegex(), "-")
        .trim()

    // 폴더 생성 - 이미 존재해도 괜찮음
    val problemFolder = File("$folderPath/${problemNumber}번 - ${safeTitle}")
    problemFolder.mkdirs()

    // 코드 파일 생성 - 이미 존재하면 건너뜀
    // 파일의 이름명르 정해 가져오기

    val codeFile = File(selectFileName(problemFolder, langExt))

    if (!codeFile.exists()) {
        codeFile.writeText(buildTemplate(problemNumber, problemData.title, langExt))
    }

    // HTML 파일 생성 - 항상 최신으로 덮어쓰기
    val htmlFile = File("${problemFolder.path}/$problemNumber.html")
    htmlFile.writeText(buildHtml(problemData))


    // 생성된 파일을 VFS에 등록 및 에디터에서 열기
    ApplicationManager.getApplication().invokeLater {
        ApplicationManager.getApplication().runWriteAction {
            LocalFileSystem.getInstance().refresh(false)
        }

        ApplicationManager.getApplication().invokeLater {
            val htmlVFile = LocalFileSystem.getInstance().findFileByPath(
                htmlFile.absolutePath.replace("\\", "/")
            )
            val codeVFile = LocalFileSystem.getInstance().findFileByPath(
                codeFile.absolutePath.replace("\\", "/")
            )

            val editorManager = com.intellij.openapi.fileEditor.ex.FileEditorManagerEx.getInstanceEx(project)

            if (codeVFile != null) {
                editorManager.openFile(codeVFile, true)
            }
            if (htmlVFile != null) {
                htmlVFile.refresh(false, false)
                try {
                    val currentWindow = editorManager.currentWindow
                    currentWindow?.split(javax.swing.SwingConstants.VERTICAL, true, htmlVFile, true)
                } catch (e: Throwable) {
                    try {
                        editorManager.currentWindow?.split(javax.swing.SwingConstants.VERTICAL, true, htmlVFile, true, false)
                    } catch (e2: Throwable) {
                        com.intellij.openapi.fileEditor.FileEditorManager.getInstance(project).openFile(htmlVFile, false)
                    }
                }
            }
            if (codeVFile != null) {
                editorManager.openFile(codeVFile, true)
            }
        }
    }
}
/**
 * 언어별 파일명 생성하는 함수
 */
private fun selectFileName(problemFolder : File, langExt: String): String {
    if(langExt == "java") {
        return "${problemFolder.path}/Main.$langExt"
    }
    else{
        return "${problemFolder.path}/app.$langExt"
    }
}

/**
 * 언어별 코드 템플릿을 생성하는 함수
 */
private fun buildTemplate(problemNumber: String, title: String, langExt: String): String {
    val date = java.time.LocalDate.now().toString()
    val url = "https://www.acmicpc.net/problem/$problemNumber"

    return when (langExt) {
        "java" -> """
//=====================================================================
//   ${problemNumber}번:    ${title}
//   @date:   $date
//   @link:   $url
//   @Motd:   폴더 내부에 있는 파일을 삭제하거나 변경하지 말아주세요.
//   @Test:   코드를 작성 후 "BOJ: 테스트"통해서 테스트를 해보세요.
//=====================================================================

import java.util.*;
import java.io.*;

public class Main {
    public static void main(String[] args) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(System.out));
        StringTokenizer st = new StringTokenizer(br.readLine(), " ");
        
        
        bw.flush();
        bw.close();
        br.close();
    }
}
""".trimIndent()

        "py" -> """
#=====================================================================
#   ${problemNumber}번:    ${title}
#   @date:   $date
#   @link:   $url
#   @Motd:   폴더 내부에 있는 파일을 삭제하거나 변경하지 말아주세요.
#   @Test:   코드를 작성 후 "BOJ: 테스트"통해서 테스트를 해보세요.
#=====================================================================

import sys

input = sys.stdin.readline

""".trimIndent()

        else -> ""
    }
}

/**
 * 문제 데이터를 HTML로 변환하는 함수
 * VSCode의 getHtml 함수와 동일한 역할
 */
internal fun buildHtml(problemData: ProblemData): String {
    return """
        <!DOCTYPE html>
        <html>
        <head>
            <meta charset="UTF-8">
            <base href="https://www.acmicpc.net/">
            <style>
                body { font-family: sans-serif; padding: 20px; background: #2b2b2b; color: #e0e0e0; }
                h1 { font-size: 24px; }
                h2 { font-size: 18px; margin-top: 20px; }
                pre { background: #3c3f41; padding: 10px; border-radius: 4px; color: #e0e0e0; }
                table { border-collapse: collapse; width: 100%; }
                td, th { border: 1px solid #555; padding: 8px; }
            </style>
        </head>
        <body>
            <h1>${problemData.title}</h1>
            ${problemData.info ?: ""}
            <h2>문제</h2>
            ${problemData.description}
            <h2>입력</h2>
            ${problemData.input}
            <h2>출력</h2>
            ${problemData.output}
            ${if (!problemData.limit.isNullOrEmpty()) "<h2>제한</h2>${problemData.limit}" else ""}
            ${buildTestCases(problemData)}
            ${if (!problemData.hint.isNullOrEmpty()) "<h2>힌트</h2>${problemData.hint}" else ""}
        </body>
        </html>
    """.trimIndent()
}

/**
 * 테스트 케이스 HTML을 생성하는 함수
 */
internal fun buildTestCases(problemData: ProblemData): String {
    val sb = StringBuilder()
    for (i in problemData.testCaseInputs.indices) {
        sb.append("<h2>예제 입력 ${i + 1}</h2>")
        sb.append("<pre>${problemData.testCaseInputs[i]}</pre>")
        sb.append("<h2>예제 출력 ${i + 1}</h2>")
        sb.append("<pre>${problemData.testCaseOutputs[i]}</pre>")
    }
    return sb.toString()
}