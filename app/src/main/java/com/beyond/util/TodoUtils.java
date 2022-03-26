package com.beyond.util;

import com.beyond.Todo;

import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author: beyond
 * @date: 2021/9/28
 */

public abstract class TodoUtils {

    public static List<Todo> parse(String content) throws IOException, ParseException {
        List<Todo> result = new ArrayList<>();
        String[] lines = StringUtils.splitPreserveAllTokens(content, "\n");
        for (String line : lines) {
            Todo parsedTodo = Todo.parseFrom(line);
            result.add(parsedTodo);
        }

        return result;
    }

    public static String format(List<Todo> todos){
        List<String> newLines = new ArrayList<>();
        for (Todo todo : todos) {
            newLines.add(todo.toFormattedLine());
        }
        return String.join("\n", newLines);
    }

}
