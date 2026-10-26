import com.sun.net.httpserver.HttpServer; // Webサーバー機能を使います。
import java.net.InetSocketAddress; // 待ち受ける番号（ポート）を指定します。
import java.net.URLDecoder; // 追加: URLの文字を日本語に戻すために使います。
import java.nio.charset.StandardCharsets; // 追加: 文字の読み方をUTF-8にそろえるために使います。

import java.util.List; // 追加: Todoの一覧を扱うListを使います。
import java.util.ArrayList; // 追加: Todoを入れるArrayListを使います。

public class App { // プログラム本体です。
    public static void main(String[] args) throws Exception { // プログラムを開始します。//【1】
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0); // 8080番でサーバーを用意します。
        // 追加: Todoを保存しておく一覧を一度だけ用意します。
        List<String> todos = new ArrayList<>();

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
                String todo = URLDecoder.decode(value, StandardCharsets.UTF_8);
                if (!todo.isEmpty()) { // 追加: 空でないTodoだけを追加します。
                    todos.add(todo); // 追加: Todoを一覧に追加します。
                } // 追加: 空かどうかの確認を終えます。
                exchange.getResponseHeaders().set("Location", "/"); // 追加: トップページへ戻る場所を指定します。
                exchange.sendResponseHeaders(303, -1); // 追加: 303（戻り先へ移動する返事）を送ります。
                exchange.close(); // 追加: 通信を閉じます。
                return; // 追加: /add の処理をここで終えます。
            } else if (path.equals("/")) { // 追加: トップページでフォームと一覧を表示します。
                String html = "<form method='post' action='/add'><input name='todo'><button>追加</button></form><ul>"; // 追加:
                                                                                                                     // 入力フォームと一覧のHTMLを始めます。
                for (String todo : todos) { // 追加: Todoを1件ずつ取り出します。
                    html += "<li>" + todo + "</li>"; // 追加: Todoを一覧の1行にします。
                } // 追加: Todoをすべて一覧にする処理を終えます。
                html += "</ul>"; // 追加: 一覧のHTMLを閉じます。
                message = html; // 追加: 表示する内容にHTMLを入れます。
                exchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8"); // 追加:
                                                                                               // HTMLとしてUTF-8で表示します。
            } else if (path.equals("/hello")) { // 追加: パスが /hello と同じか比べます。
                String query = exchange.getRequestURI().getRawQuery(); // 追加: クエリ（URLの ? より後ろ）を取り出します。
                System.out.println("query = " + query);
                String name = query == null ? "ゲスト" : query.substring(5); // 修正: クエリがないときはゲスト、それ以外は name= の後ろを切り出します。
                message = "こんにちは、" + name + "さん！"; // 追加: 名前を入れたあいさつにします。
            } else if (path.equals("/menu")) { // 追加: パスが /bye と同じか比べます。
                message = "今日のお昼はビリヤニさん";
            } else if (path.equals("/bye")) { // 追加: パスが /bye と同じか比べます。
                message = "さようなら！";
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
