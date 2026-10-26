import com.sun.net.httpserver.HttpServer; // Webサーバー機能を使います。
import java.net.InetSocketAddress; // 待ち受ける番号（ポート）を指定します。
import java.net.URLDecoder; // 追加: URLの文字を日本語に戻すために使います。
import java.nio.charset.StandardCharsets; // 追加: 文字の読み方をUTF-8にそろえるために使います。

import java.util.List; // 追加: Todoの一覧を扱うListを使います。
import java.util.ArrayList; // 追加: Todoを入れるArrayListを使います。

public class App { // プログラム本体です。
    static List<Todo> todos = new ArrayList<>(); // ★変更
    static int nextId = 1; // ★変更

    public static void main(String[] args) throws Exception { // プログラムを開始します。//【1】
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0); // 8080番でサーバーを用意します。
        todos.add(new Todo(nextId++, "牛乳を買う")); // ★変更
        Todo egg = new Todo(nextId++, "卵を買う"); // ★変更
        egg.setDone(true); // ★変更
        todos.add(egg); // ★変更

        server.createContext("/", exchange -> { // 「/」へのアクセスを受け取ります。//【1】
            String path = exchange.getRequestURI().getPath(); // 追加: アクセスされたパス（住所の末尾）を取り出します。
            // 追加: GETかPOSTかを取り出します。
            String method = exchange.getRequestMethod();
            String message;
            exchange.getResponseHeaders().set("Content-Type", "text/plain; charset=UTF-8"); // 追加:
                                                                                            // 基本の返答形式を文字だけのUTF-8にします。
            if (path.equals("/add") && method.equals("POST")) { // 追加: Todoを追加するPOST送信を受け取ります。
                // 追加: フォームから送られた内容をUTF-8で受け取ります。
                String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
                // 追加: todo= の5文字を除いた値を取り出します。
                String value = body.substring(5);
                // 追加: URL形式の日本語を元の文字に戻します。
                String title = URLDecoder.decode(value, StandardCharsets.UTF_8); // ★変更
                if (!title.isEmpty()) { // ★変更
                    todos.add(new Todo(nextId, title)); // ★変更
                    nextId++; // ★変更
                } // 追加: 空かどうかの確認を終えます。
                exchange.getResponseHeaders().set("Location", "/"); // 追加: トップページへ戻る場所を指定します。
                exchange.sendResponseHeaders(303, -1); // 追加: 303（戻り先へ移動する返事）を送ります。
                exchange.close(); // 追加: 通信を閉じます。
                return; // 追加: /add の処理をここで終えます。
            } else if (path.equals("/done") && method.equals("GET")) { // ★追加
                String query = exchange.getRequestURI().getQuery(); // ★追加
                if (query != null && query.startsWith("id=") && query.length() > 3) { // ★追加
                    try { // ★追加
                        int id = Integer.parseInt(query.substring(3)); // ★追加
                        for (Todo todo : todos) { // ★追加
                            if (todo.getId() == id) { // ★追加
                                todo.setDone(true); // ★追加
                                break; // ★追加
                            } // ★追加
                        } // ★追加
                    } catch (NumberFormatException e) { // ★追加
                    } // ★追加
                } // ★追加
                exchange.getResponseHeaders().set("Location", "/"); // ★追加
                exchange.sendResponseHeaders(303, -1); // ★追加
                return; // ★追加
            } else if (path.equals("/delete") && method.equals("GET")) { // ★追加
                String query = exchange.getRequestURI().getQuery(); // ★追加
                if (query != null && query.startsWith("id=") && query.length() > 3) { // ★追加
                    try { // ★追加
                        int id = Integer.parseInt(query.substring(3)); // ★追加
                        todos.removeIf(todo -> todo.getId() == id); // ★追加
                    } catch (NumberFormatException e) { // ★追加
                    } // ★追加
                } // ★追加
                exchange.getResponseHeaders().set("Location", "/"); // ★追加
                exchange.sendResponseHeaders(303, -1); // ★追加
                return; // ★追加
            } else if (path.equals("/")) { // 追加: トップページでフォームと一覧を表示します。
                String html = "<form method='post' action='/add'><input name='todo'><button>追加</button></form><ul>"; // 追加:
                                                                                                                     // 入力フォームと一覧のHTMLを始めます。
                for (Todo todo : todos) { // ★変更
                    String mark = ""; // ★変更
                    if (todo.isDone()) { // ★変更
                        mark = " ✔"; // ★変更
                    } // ★変更
                    html += "<li>" + todo.getTitle() + mark + " <a href='/done?id=" + todo.getId()
                            + "'>完了</a> <a href='/delete?id=" + todo.getId() + "'>削除</a></li>"; // ★追加
                } // 追加: Todoをすべて一覧にする処理を終えます。
                html += "</ul>"; // 追加: 一覧のHTMLを閉じます。
                message = html; // 追加: 表示する内容にHTMLを入れます。
                exchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8"); // 追加:
                                                                                               // HTMLとしてUTF-8で表示します。
            } else {
                message = "ページが見つかりません";
            }
            /*
             * String message = "包囲されています( ﾟДﾟ)!"; // ブラウザへ返す文字です。//【毎】
             */
            byte[] body = message.getBytes("UTF-8"); // 文字を送信できるデータにします。
            exchange.sendResponseHeaders(200, body.length); // 成功（200）として送信を始めます。
            exchange.getResponseBody().write(body); // 文字を送り返します。
            exchange.getResponseBody().close(); // 送信を完了します。
        }); // 「/」への処理を登録します。
        server.start(); // サーバーを起動します。
        System.out.println("サーバー起動: http://localhost:8080 （止めるときは Ctrl+C）"); // 起動した場所を表示します。
    } // 開始処理を終えます。
} // プログラム本体を終えます。

class Todo { // ★変更
    private final int id; // ★変更
    private final String title; // ★変更
    private boolean done; // ★変更

    Todo(int id, String title) { // ★変更
        this.id = id; // ★変更
        this.title = title; // ★変更
        this.done = false; // ★変更
    } // ★変更

    int getId() { // ★変更
        return id; // ★変更
    } // ★変更

    String getTitle() { // ★変更
        return title; // ★変更
    } // ★変更

    boolean isDone() { // ★変更
        return done; // ★変更
    } // ★変更

    void setDone(boolean done) { // ★変更
        this.done = done; // ★変更
    } // ★変更
} // ★変更
