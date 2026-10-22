
// List（複数のデータを順番に入れる箱）を使うために読み込みます。
import java.util.List;

// ArrayList（増やせるリスト）を使うために読み込みます。
import java.util.ArrayList;

// Todo（やること1件）を表すクラスです。
class Todo {
    // title（やることの題名）を保存します。
    private final String title;

    // done（完了したかどうか）を保存します。
    private final boolean done;

    Todo(String title, boolean done) {
        // 受け取った題名を保存します。
        this.title = title;
        // 受け取った完了状態を保存します。
        this.done = done;
    }

    // TodoをHTMLのli（リストの1項目）形式の文字列に変えます。
    String toItem() {
        // 完了済みなら「[済]」を付けた文字列を返します。
        if (done) {
            // 完了済みの表示を返します。
            return "<li>[(｀・ω・´)ｼｬｷｰﾝ] " + title + "</li>";
        }
        // 未完了の表示を返します。
        return "<li>" + title + "</li>";
    }
}

// Javaを開始するためのMainクラスです。
public class Main {
    // プログラムが最初に実行するmainメソッド（処理の入口）です。
    public static void main(String[] args) {
        // todosという名前で、Todoを入れるリストを作ります。
        List<Todo> todos = new ArrayList<>();

        // 未完了の「牛乳を買う」をリストに追加します。
        todos.add(new Todo("牛乳を買う", false));

        // 未完了の「豚さんを買う」をリストに追加します。
        todos.add(new Todo("豚さんを買う", true));

        // 完了済みの「ゴミを出す」をリストに追加します。
        todos.add(new Todo("ゴミを出す", true));

        // for（同じ処理を繰り返す書き方）で、リストの各Todoを順番に取り出します。
        for (Todo todo : todos) {
            // toItemの結果をターミナルに1行ずつ表示します。
            System.out.println(todo.toItem());
        }
    }
}
