package org.ekstep.content.util

import java.util


object LanguageCode {
  private val languageMap = new util.HashMap[String, String]

  def getLanguageCode(language: String): String = languageMap.get(language)

  languageMap.put("assamese", "as")
  languageMap.put("bengali", "bn")
  languageMap.put("english", "en")
  languageMap.put("gujarati", "gu")
  languageMap.put("hindi", "hi")
  languageMap.put("kannada", "ka")
  languageMap.put("marathi", "mr")
  languageMap.put("odia", "or")
  languageMap.put("tamil", "ta")
  languageMap.put("telugu", "te")

}
