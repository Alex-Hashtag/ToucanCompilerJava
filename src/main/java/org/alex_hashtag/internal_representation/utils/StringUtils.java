package org.alex_hashtag.internal_representation.utils;

public class StringUtils
{
    public static void appendIndented(StringBuilder sb, int indentLevel, String text)
    {
        String indent = "    ".repeat(indentLevel); // 4 spaces per indent level
        sb.append(indent).append(text);
    }
}
