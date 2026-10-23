import com.sun.net.httpserver.HttpServer; // Webサーバー機能を使います。
import java.net.InetSocketAddress; // 待ち受ける番号（ポート）を指定します。

import java.util.List; // 追加: Todoの一覧を扱うListを使います。
import java.util.ArrayList; // 追加: Todoを入れるArrayListを使います。

public class App { // プログラム本体です。
    public static void main(String[] args) throws Exception { // プログラムを開始します。//【1】
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0); // 8080番でサーバーを用意します。
        server.createContext("/", exchange -> { // 「/」へのアクセスを受け取ります。//【1】
            String path = exchange.getRequestURI().getPath(); // 追加: アクセスされたパス（住所の末尾）を取り出します。
            String message;
            exchange.getResponseHeaders().set("Content-Type", "text/plain; charset=UTF-8"); // 追加:
                                                                                            // 基本の返答形式を文字だけのUTF-8にします。
            if (path.equals("/hello")) { // 追加: パスが /hello と同じか比べます。
                String query = exchange.getRequestURI().getRawQuery(); // 追加: クエリ（URLの ? より後ろ）を取り出します。
                System.out.println("query = " + query);
                String name = query == null ? "ゲスト" : query.substring(5); // 修正: クエリがないときはゲスト、それ以外は name= の後ろを切り出します。
                message = "こんにちは、" + name + "さん！"; // 追加: 名前を入れたあいさつにします。
            } else if (path.equals("/todos")) { // 追加: パスが /todos と同じか比べます。
                List<String> todos = new ArrayList<>(); // 追加: Todoを入れる一覧を作ります。
                todos.add("牛乳を買う"); // 追加: 1件目のTodoを入れます。
                todos.add("卵を買う"); // 追加: 2件目のTodoを入れます。
                todos.add("パンを買う"); // 追加: 3件目のTodoを入れます。
                String html = "<ul>"; // 追加: HTMLのリストを始めます。
                for (String todo : todos) { // 追加: Todoを1件ずつ取り出します。
                    html += "<li>" + todo + "</li>"; // 追加: TodoをHTMLの項目にします。
                }
                html += "</ul>"; // 追加: HTMLのリストを閉じます。
                message = html; // 追加: 組み立てたHTMLを返す内容にします。
                exchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8"); // 追加: /todos
                                                                                               // だけHTMLとしてUTF-8で返します。
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
