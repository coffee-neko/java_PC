import com.sun.net.httpserver.HttpServer; // Webサーバー機能を使います。
import java.net.InetSocketAddress; // 待ち受ける番号（ポート）を指定します。
import java.net.URLDecoder; // 追加: URLの文字を日本語に戻すために使います。
import java.nio.charset.StandardCharsets; // 追加: 文字の読み方をUTF-8にそろえるために使います。
import java.sql.Connection; // ★ SQLiteへの接続を扱うために使います。
import java.sql.DriverManager; // ★ JDBCでデータベースへ接続するために使います。
import java.sql.PreparedStatement; // ★ SQLを安全に実行するために使います。
import java.sql.ResultSet; // ★ SELECTの検索結果を受け取るために使います。
import java.sql.SQLException; // ★ データベース操作のエラーを扱うために使います。

import java.util.List; // 追加: Todoの一覧を扱うListを使います。
import java.util.ArrayList; // 追加: Todoを入れるArrayListを使います。

public class App { // プログラム本体です。
    static final String DB_URL = "jdbc:sqlite:todos.db"; // ★ プロジェクトの実行フォルダにtodos.dbを作る接続先です。

    public static void main(String[] args) throws Exception { // プログラムを開始します。//【1】
        createTableIfNeeded(); // ★ 起動時にtodos表がなければ作成します。
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0); // 8080番でサーバーを用意します。

        server.createContext("/api/todos", exchange -> { // JSON形式でTodo一覧を返すAPIを登録します。
            if (!exchange.getRequestMethod().equals("GET")) { // GET以外のリクエストか確認します。
                exchange.sendResponseHeaders(405, -1); // 許可されないメソッドとして405を返します。
                exchange.close(); // 通信を閉じます。
                return; // API処理を終えます。
            } // HTTPメソッドの確認を終えます。
            List<Todo> todos = selectTodos(); // データベースから全Todoを読み込みます。
            StringBuilder json = new StringBuilder("["); // JSON配列の開始文字を入れます。
            for (int i = 0; i < todos.size(); i++) { // Todoを1件ずつJSONへ変換します。
                Todo todo = todos.get(i); // 現在のTodoを取り出します。
                if (i > 0) { // 先頭以外の要素か確認します。
                    json.append(","); // JSON配列の要素区切りカンマを追加します。
                } // 要素区切りの追加を終えます。
                json.append("{\"title\":\""); // titleプロパティの開始部分を追加します。
                json.append(escapeJson(todo.getTitle())); // タイトルをJSON用にエスケープして追加します。
                json.append("\",\"done\":"); // doneプロパティの開始部分を追加します。
                json.append(todo.isDone()); // 完了状態をtrueまたはfalseで追加します。
                json.append("}"); // Todoオブジェクトを閉じます。
            } // 全TodoのJSON変換を終えます。
            json.append("]"); // JSON配列を閉じます。
            byte[] body = json.toString().getBytes(StandardCharsets.UTF_8); // JSONをUTF-8の送信データへ変換します。
            exchange.getResponseHeaders().set("Content-Type", "application/json"); // Content-Typeをcharsetなしのapplication/jsonにします。
            exchange.sendResponseHeaders(200, body.length); // 成功（200）として送信を始めます。
            exchange.getResponseBody().write(body); // JSONをAPI利用側へ送ります。
            exchange.getResponseBody().close(); // 送信を完了します。
        }); // /api/todos の処理登録を終えます。

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
                String title = URLDecoder.decode(value, StandardCharsets.UTF_8); // URL形式の日本語を元の文字に戻します。
                if (!title.isEmpty()) { // 空のTodoでないか確認します。
                    insertTodo(title); // ★ INSERTでTodoをデータベースへ追加します。
                } // 追加: 空かどうかの確認を終えます。
                exchange.getResponseHeaders().set("Location", "/"); // 追加: トップページへ戻る場所を指定します。
                exchange.sendResponseHeaders(303, -1); // 追加: 303（戻り先へ移動する返事）を送ります。
                exchange.close(); // 追加: 通信を閉じます。
                return; // 追加: /add の処理をここで終えます。
            } else if (path.equals("/done") && method.equals("GET")) { // Todoを完了にする送信を受け取ります。
                String query = exchange.getRequestURI().getQuery(); // URLのidを取り出します。
                if (query != null && query.startsWith("id=") && query.length() > 3) { // idがあるか確認します。
                    try { // idの数値変換エラーを処理します。
                        int id = Integer.parseInt(query.substring(3)); // idを数値に変換します。
                        updateTodoDone(id, true); // ★ UPDATEで指定したTodoを完了にします。
                    } catch (NumberFormatException e) { // idが数値でない場合を受け取ります。
                    } // id変換処理を終えます。
                } // id確認を終えます。
                exchange.getResponseHeaders().set("Location", "/"); // トップページへ戻る場所を指定します。
                exchange.sendResponseHeaders(303, -1); // 303でトップページへの移動を返します。
                return; // /doneの処理をここで終えます。
            } else if (path.equals("/undone") && method.equals("GET")) { // 追加: 完了を未完了に戻す送信を受け取ります。
                String query = exchange.getRequestURI().getQuery(); // 追加: URLの id を取り出します。
                if (query != null && query.startsWith("id=") && query.length() > 3) { // 追加: id があるか確認します。
                    try {
                        int id = Integer.parseInt(query.substring(3)); // 追加: id を数にします。
                        updateTodoDone(id, false); // ★ UPDATEで指定したTodoを未完了に戻します。
                    } catch (NumberFormatException e) {
                    }
                }
                exchange.getResponseHeaders().set("Location", "/"); // 追加: トップページへ戻る場所を指定します。
                exchange.sendResponseHeaders(303, -1); // 追加: 戻り先へ移動する返事を送ります。
                return; // 追加: /undone の処理をここで終えます。
            } else if (path.equals("/delete") && method.equals("GET")) { // Todoを削除する送信を受け取ります。
                String query = exchange.getRequestURI().getQuery(); // URLのidを取り出します。
                if (query != null && query.startsWith("id=") && query.length() > 3) { // idがあるか確認します。
                    try { // idの数値変換エラーを処理します。
                        int id = Integer.parseInt(query.substring(3)); // idを数値に変換します。
                        deleteTodo(id); // ★ DELETEで指定したTodoをデータベースから削除します。
                    } catch (NumberFormatException e) { // idが数値でない場合を受け取ります。
                    } // id変換処理を終えます。
                } // id確認を終えます。
                exchange.getResponseHeaders().set("Location", "/"); // トップページへ戻る場所を指定します。
                exchange.sendResponseHeaders(303, -1); // 303でトップページへの移動を返します。
                return; // /deleteの処理をここで終えます。
            } else if (path.equals("/")) { // 追加: トップページでフォームと一覧を表示します。
                List<Todo> todos = selectTodos(); // ★ SELECTでデータベースから最新のTodo一覧を読み込みます。
                String html = "<html><head><meta charset='UTF-8'><style>body { max-width: 600px; margin: 24px auto; font-size: 16px; }</style></head><body><h1>📝 今日のやること</h1><form method='post' action='/add'><input name='todo'><button>追加</button></form>"; // 追加: 入力フォームを始めます。
                if (todos.isEmpty()) {
                    html += "<p>やることないです。自由です！追加するのも、、、自由です。。</p>";
                } else {
                    html += "<ul>"; // 追加: 未完了の一覧のHTMLを始めます。
                    for (Todo todo : todos) {
                        if (!todo.isDone()) { // 追加: 未完了のTodoだけを表示します。
                            html += "<li>" + todo.getTitle() + " <a href='/done?id=" + todo.getId()
                                    + "'>完了</a> <a href='/delete?id=" + todo.getId() + "'>削除</a></li>";
                        }
                    }
                    html += "</ul><h2>✅ 完了したこと</h2><ul>"; // 追加: 完了したTodoの一覧を始めます。
                    for (Todo todo : todos) { // Todoを1件ずつ表示します。
                        String mark = ""; // 完了マークを用意します。
                        if (todo.isDone()) { // 完了済みTodoか確認します。
                            mark = " ✔"; // 完了マークを設定します。
                            html += "<li>" + todo.getTitle() + mark + " <a href='/undone?id=" + todo.getId()
                                    + "'>未完了に戻す</a> <a href='/delete?id=" + todo.getId() + "'>削除</a></li>"; // 追加: 完了したTodoを一覧の1行にします。
                        } // 完了Todoの表示を終えます。
                    } // 追加: Todoをすべて一覧にする処理を終えます。
                    html += "</ul>"; // 追加: 一覧のHTMLを閉じます。
                }
                html += "</body></html>";
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

    static void createTableIfNeeded() { // ★ todos表がなければ作成します。
        String sql = "CREATE TABLE IF NOT EXISTS todos (id INTEGER PRIMARY KEY, title TEXT, done INTEGER)"; // ★ 必要なtodos表を定義します。
        try (Connection connection = DriverManager.getConnection(DB_URL); // ★ SQLiteのtodos.dbへ接続します。
                PreparedStatement statement = connection.prepareStatement(sql)) { // ★ CREATE TABLE文を準備します。
            statement.execute(); // ★ todos表を作成します。すでにあれば何もしません。
        } catch (SQLException e) { // ★ データベース初期化のエラーを受け取ります。
            throw new IllegalStateException("todos表を準備できません", e); // ★ 初期化失敗を知らせます。
        } // ★ 表の準備処理を終えます。
    } // ★ todos表の準備メソッドを終えます。

    static void insertTodo(String title) { // ★ INSERTでTodoを追加します。
        String sql = "INSERT INTO todos (title, done) VALUES (?, ?)"; // ★ 追加用のSQLを用意します。
        try (Connection connection = DriverManager.getConnection(DB_URL); // ★ SQLiteのtodos.dbへ接続します。
                PreparedStatement statement = connection.prepareStatement(sql)) { // ★ INSERT文を準備します。
            statement.setString(1, title); // ★ 1個目の?にタイトルを設定します。
            statement.setInt(2, 0); // ★ 2個目の?に未完了を表す0を設定します。
            statement.executeUpdate(); // ★ INSERTを実行します。
        } catch (SQLException e) { // ★ Todo追加時のエラーを受け取ります。
            throw new IllegalStateException("Todoを追加できません", e); // ★ 追加失敗を知らせます。
        } // ★ INSERT処理を終えます。
    } // ★ Todo追加メソッドを終えます。

    static void updateTodoDone(int id, boolean done) { // ★ UPDATEでTodoの完了状態を変更します。
        String sql = "UPDATE todos SET done = ? WHERE id = ?"; // ★ 完了状態更新用のSQLを用意します。
        try (Connection connection = DriverManager.getConnection(DB_URL); // ★ SQLiteのtodos.dbへ接続します。
                PreparedStatement statement = connection.prepareStatement(sql)) { // ★ UPDATE文を準備します。
            statement.setInt(1, done ? 1 : 0); // ★ 1個目の?に完了なら1、未完了なら0を設定します。
            statement.setInt(2, id); // ★ 2個目の?に対象TodoのIDを設定します。
            statement.executeUpdate(); // ★ UPDATEを実行します。
        } catch (SQLException e) { // ★ Todo更新時のエラーを受け取ります。
            throw new IllegalStateException("Todoを更新できません", e); // ★ 更新失敗を知らせます。
        } // ★ UPDATE処理を終えます。
    } // ★ Todo更新メソッドを終えます。

    static void deleteTodo(int id) { // ★ DELETEでTodoを削除します。
        String sql = "DELETE FROM todos WHERE id = ?"; // ★ 削除用のSQLを用意します。
        try (Connection connection = DriverManager.getConnection(DB_URL); // ★ SQLiteのtodos.dbへ接続します。
                PreparedStatement statement = connection.prepareStatement(sql)) { // ★ DELETE文を準備します。
            statement.setInt(1, id); // ★ 1個目の?に削除対象のIDを設定します。
            statement.executeUpdate(); // ★ DELETEを実行します。
        } catch (SQLException e) { // ★ Todo削除時のエラーを受け取ります。
            throw new IllegalStateException("Todoを削除できません", e); // ★ 削除失敗を知らせます。
        } // ★ DELETE処理を終えます。
    } // ★ Todo削除メソッドを終えます。

    static List<Todo> selectTodos() { // ★ SELECTでTodo一覧を読み込みます。
        List<Todo> todos = new ArrayList<>(); // ★ 読み込んだTodoを入れる一覧を用意します。
        String sql = "SELECT id, title, done FROM todos ORDER BY id"; // ★ 一覧取得用のSQLを用意します。
        try (Connection connection = DriverManager.getConnection(DB_URL); // ★ SQLiteのtodos.dbへ接続します。
                PreparedStatement statement = connection.prepareStatement(sql); // ★ SELECT文を準備します。
                ResultSet result = statement.executeQuery()) { // ★ SELECTを実行して結果を受け取ります。
            while (result.next()) { // ★ 検索結果を1件ずつ読み込みます。
                Todo todo = new Todo(result.getInt("id"), result.getString("title")); // ★ IDとタイトルからTodoを作ります。
                todo.setDone(result.getInt("done") == 1); // ★ doneが1なら完了状態にします。
                todos.add(todo); // ★ 読み込んだTodoを一覧へ追加します。
            } // ★ 全検索結果の読込を終えます。
        } catch (SQLException e) { // ★ Todo一覧取得時のエラーを受け取ります。
            throw new IllegalStateException("Todo一覧を読み込めません", e); // ★ 読込失敗を知らせます。
        } // ★ SELECT処理を終えます。
        return todos; // ★ データベースから読み込んだTodo一覧を返します。
    } // ★ Todo一覧取得メソッドを終えます。

    static String escapeJson(String value) { // 文字列をJSONで安全に使える形へ変換します。
        StringBuilder escaped = new StringBuilder(); // エスケープ後の文字列を組み立てます。
        for (int i = 0; i < value.length(); i++) { // タイトルを1文字ずつ確認します。
            char c = value.charAt(i); // 現在の1文字を取り出します。
            switch (c) { // JSONで特別な扱いが必要な文字か分けます。
                case '"': // ダブルクォートの場合です。
                    escaped.append("\\\""); // ダブルクォートをJSON用にエスケープします。
                    break; // この文字の処理を終えます。
                case '\\': // バックスラッシュの場合です。
                    escaped.append("\\\\"); // バックスラッシュをJSON用にエスケープします。
                    break; // この文字の処理を終えます。
                case '\b': // バックスペースの場合です。
                    escaped.append("\\b"); // バックスペースをJSON用にエスケープします。
                    break; // この文字の処理を終えます。
                case '\f': // 改ページの場合です。
                    escaped.append("\\f"); // 改ページをJSON用にエスケープします。
                    break; // この文字の処理を終えます。
                case '\n': // 改行の場合です。
                    escaped.append("\\n"); // 改行をJSON用にエスケープします。
                    break; // この文字の処理を終えます。
                case '\r': // 復帰の場合です。
                    escaped.append("\\r"); // 復帰をJSON用にエスケープします。
                    break; // この文字の処理を終えます。
                case '\t': // タブの場合です。
                    escaped.append("\\t"); // タブをJSON用にエスケープします。
                    break; // この文字の処理を終えます。
                default: // 上記以外の文字の場合です。
                    if (c < 0x20) { // JSONでそのまま使えない制御文字か確認します。
                        escaped.append(String.format("\\u%04x", (int) c)); // 制御文字をUnicodeエスケープへ変換します。
                    } else { // 通常の文字の場合です。
                        escaped.append(c); // 文字をそのまま追加します。
                    } // 通常文字か制御文字かの処理を終えます。
            } // 文字ごとの分岐を終えます。
        } // 全文字の変換を終えます。
        return escaped.toString(); // JSON用に安全な文字列を返します。
    } // JSON文字列エスケープメソッドを終えます。
} // プログラム本体を終えます。

class Todo { // Todoを表すクラスです。
    private final int id; // TodoのIDです。
    private final String title; // Todoのタイトルです。
    private boolean done; // Todoが完了済みかを表します。

    Todo(int id, String title) { // Todoを作ります。
        this.id = id; // IDを設定します。
        this.title = title; // タイトルを設定します。
        this.done = false; // 最初は未完了にします。
    } // コンストラクタを終えます。

    int getId() { // IDを返します。
        return id; // IDを返します。
    } // getIdを終えます。

    String getTitle() { // タイトルを返します。
        return title; // タイトルを返します。
    } // getTitleを終えます。

    boolean isDone() { // 完了状態を返します。
        return done; // 完了状態を返します。
    } // isDoneを終えます。

    void setDone(boolean done) { // 完了状態を設定します。
        this.done = done; // 完了状態を保存します。
    } // setDoneを終えます。
} // Todoクラスを終えます。
