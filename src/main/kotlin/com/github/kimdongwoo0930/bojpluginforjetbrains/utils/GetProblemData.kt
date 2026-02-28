package com.github.kimdongwoo0930.bojpluginforjetbrains.utils

import org.jsoup.Jsoup

/**
 * 백준 문제 데이터를 담는 데이터 클래스
 * VSCode의 problemData 타입과 동일한 역할
 */
data class ProblemData(
    val title: String,
    val description: String,
    val input: String,
    val output: String,
    val testCaseInputs: List<String>,
    val testCaseOutputs: List<String>,
    val info: String?,
    val limit: String?,
    val hint: String?,
)

/**
 * 백준 문제 번호로 문제 데이터를 스크래핑하는 함수
 * VSCode의 getProblemData 함수와 동일한 역할
 * @param number 문제 번호
 * @return ProblemData 또는 null (문제가 없을 경우)
 */
fun getProblemData(number: String): ProblemData? {
    return try {
        val url = "https://www.acmicpc.net/problem/$number"
        val doc = Jsoup.connect(url)
            .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/58.0.3029.110 Safari/537.3")
            .header("Accept-Language", "ko-KR,ko;q=0.9")
            .get()

        val title = doc.select("#problem_title").text()
        val description = doc.select("#problem_description").html().replace("\t", "")
        val input = doc.select("#problem_input").html().replace("\t", "")
        val output = doc.select("#problem_output").html().replace("\t", "")
        val info = doc.select("#problem-info").outerHtml()
        val limit = doc.select("#problem_limit").html()
        val hint = doc.select("#problem_hint").html()

        // 테스트 케이스 추출
        val testCaseInputs = mutableListOf<String>()
        val testCaseOutputs = mutableListOf<String>()

        var index = 1
        while (true) {
            val input_ = doc.select("#sample-input-$index").first()?.text() ?: break
            val output_ = doc.select("#sample-output-$index").first()?.text() ?: break
            if (input_.isEmpty() || output_.isEmpty()) break

            testCaseInputs.add(input_)
            testCaseOutputs.add(output_)
            index++
        }

        ProblemData(
            title = title,
            description = description,
            input = input,
            output = output,
            testCaseInputs = testCaseInputs,
            testCaseOutputs = testCaseOutputs,
            info = info,
            limit = limit,
            hint = hint,
        )
    } catch (e: Exception) {
        null
    }
}