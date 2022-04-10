package com.beyond.server;

import android.util.Log;

import com.beyond.Todo;
import com.beyond.jgit.util.JsonUtils;
import com.beyond.jgit.util.PathUtils;
import com.beyond.util.CalendarUtils;
import com.beyond.util.TodoUtils;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

import fi.iki.elonen.NanoHTTPD;

public class HttpServer extends NanoHTTPD {

    private final HttpServerService httpServerService;

    public HttpServer(HttpServerService serverService) throws IOException {
        super(8080);
        start(NanoHTTPD.SOCKET_READ_TIMEOUT, false);
        Log.i("HttpServer", "HttpServer: Running! Point your browsers to http://localhost:8080/ ");
        this.httpServerService = serverService;
    }

    @Override
    public Response serve(IHTTPSession session) {
        Map<String, List<String>> parameters = session.getParameters();
        List<String> data = parameters.get("data");
        if (data == null){
            String msg = JsonUtils.writeValueAsString(Collections.singletonMap("status", 1 ));
            return newFixedLengthResponse(Response.Status.OK, NanoHTTPD.MIME_PLAINTEXT, msg);
        }
        String repoRoot = PathUtils.concat(httpServerService.getFilesDir().getAbsolutePath(), "test-repo");
        String name = DateFormatUtils.format(new Date(), "yyyyMMdd_HHmm");
        String content = String.join("\n", data);
        add(repoRoot, name, content);
        String msg = JsonUtils.writeValueAsString(Collections.singletonMap("status", 0));
        return newFixedLengthResponse(Response.Status.OK, NanoHTTPD.MIME_PLAINTEXT, msg);
    }

    private void add(String repoRoot, String name, String content) {
        if (StringUtils.isBlank(name) || StringUtils.isBlank(repoRoot)) {
            return;
        }
        File file = new File(PathUtils.concat(repoRoot, name));
        try {
            if (file.exists()){
                content = FileUtils.readFileToString(file, StandardCharsets.UTF_8) + "\n" + content;
            }
            if (StringUtils.isNotBlank(content)) {
                List<Todo> parsed = TodoUtils.parse(content);
                boolean createdNewReminder = false;
                for (Todo todo : parsed) {
                    createdNewReminder = createdNewReminder || this.setupReminder(todo);
                }
                String newContent = TodoUtils.format(parsed);
                if (!name.endsWith(".todo") && createdNewReminder) {
                    File todoFile = new File(PathUtils.concat(repoRoot, name + ".todo"));
                    FileUtils.writeStringToFile(todoFile, newContent, StandardCharsets.UTF_8);
                    FileUtils.deleteQuietly(file);
                }else{
                    FileUtils.writeStringToFile(file, newContent, StandardCharsets.UTF_8);
                }
            }
        } catch (IOException | ParseException e) {
            e.printStackTrace();
        }
    }

    private boolean setupReminder(Todo todo) {
        if (todo.getRemindTime() != null && todo.getSource() != Todo.Source.READ) {
            CalendarUtils.insertEventAndReminder(httpServerService.getApplicationContext(), todo.getOriginText(), null, todo.getRemindTime());
            return true;
        }
        return false;
    }
}
