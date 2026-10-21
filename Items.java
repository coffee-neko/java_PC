public class Items { // Itemsという名前のプログラムのまとまり（クラス）を作ります
    public static void main(String[] args) { // プログラムを開始する場所（mainメソッド）です
        String[] todos = { "牛乳を買う", "卵を買う", "パンを買う", "掃除をする" }; // Todoの文字列を入れる配列（複数の値を並べて入れる箱）です
        for (int i = 0; i < todos.length; i++) { // iで先頭から順に数え、配列の最後まで繰り返します
            System.out.println("<li>" + todos[i] + "</li>"); // 今のTodoを<li>と</li>で囲んで1行表示します
        } // 繰り返し処理（for文）の終わりです
    } // mainメソッドの終わりです
} // Itemsクラスの終わりです
