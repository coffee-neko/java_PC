public class Items { // Itemsという名前のプログラムのまとまり（クラス）を作ります
    public static void main(String[] args) { // プログラムを開始する場所（mainメソッド）です
        String[] todos = { "牛乳を買う", "", "パンを買う", "掃除をする" }; // Todoの文字列を入れる配列（複数の値を並べて入れる箱）です
        boolean[] done = { true, false, false, false }; // Todoが完了したかを入れる配列（trueは完了、falseは未完了）です
        for (int i = 0; i < todos.length; i++) { // iで先頭から順に数え、配列の最後まで繰り返します
            if (!todos[i].isEmpty()) { // 今のTodoが空文字（文字が1つもない値）ではないか確認します
                if (done[i]) { // 今のTodoが完了しているか確認します
                    System.out.println("<li>[済] " + todos[i] + "</li>"); // 完了したTodoには[済]を付けて1行表示します
                } else { // 完了していない場合の処理です
                    System.out.println("<li>" + todos[i] + "</li>"); // 未完了のTodoをそのまま1行表示します
                } // 完了したかどうかを確認するif文の終わりです
            } // if文の終わりです
        } // 繰り返し処理（for文）の終わりです
    } // mainメソッドの終わりです
} // Itemsクラスの終わりです
