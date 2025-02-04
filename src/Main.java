import java.sql.*;
import java.util.ArrayList;
import java.util.Scanner;

/* For some reason, Mockaroo only has movie titles and
 * not book titles, so the titles are movie names */

public class Main {
  static Scanner in = new Scanner(System.in);

  public static void main(String[] args) {
    /*
     * try {
     * Connection connection =
     * DriverManager.getConnection("jdbc:postgresql:library");
     * PreparedStatement stmt = connection.prepareStatement("");
     * } catch (SQLException ex) {
     * ex.printStackTrace();
     * System.exit(1);
     * }
     */
    System.out.println("Welcome to the Java Library!");

    String option = "";

    while (!option.equalsIgnoreCase("q")) {
      System.out
          .println("Would you like to [register] for a library card, [borrow] a book, [return] a book?, or [q]uit?");
      System.out.print("> ");
      option = in.nextLine();

      switch (option.toLowerCase()) {
        case "register":
          register();
          break;
        case "borrow":
          borrowBook();
          break;
        case "return":
          returnBook();
          break;
        case "q":
          break;
        default:
          System.out.println("Invalid option");
      }
    }
  }

  public static void register() {

    System.out.println("What's your first name?");
    System.out.print("> ");
    String fname = in.nextLine();

    System.out.println("And your last name?");
    System.out.print("> ");
    String lname = in.nextLine();

    Person person = new Person(fname, lname);
    person.save();
    System.out.println("Thanks for registering! Your ID is " + person.id);
  }

  public static void borrowBook() {

    String id_str = "-1";
    int id_int = -1;
    boolean success = false;

    id_int = getId(id_int, success, in);

    System.out.println("What's the title of the book you'd like to borrow?");
    System.out.print("> ");
    String title = in.nextLine();

    boolean bookSuccess = false;
    ArrayList<Book> books = Book.find(title);
    if (!books.isEmpty()) {
      for (Book book : books) {
        System.out
            .println("Is " + book.title + " by " + book.author + "the book you want? ([yes] / [no])");
        System.out.print("> ");
        String ans = in.nextLine();
        if (ans.equalsIgnoreCase("yes")) {
          book.borrowBook(id_int);
          bookSuccess = true;
          break;
        }
      }
      if (!bookSuccess) {
        System.out.println("Sorry, we don't have that book");
      }
    }
  }

  private static int getId(int id_int, boolean success, Scanner in) {
    String id_str;
    System.out.println("What's your ID number?");
    System.out.print("> ");

    while (!success) {
      id_str = in.nextLine();
      try {
        id_int = Integer.parseInt(id_str);
        success = true;
      } catch (Exception e) {
        System.out.println("Invalid ID number, Try Again");
        System.out.print("> ");
      }
    }
    return id_int;
  }

  public static void returnBook() {
    Scanner return_in = new Scanner(System.in);

    String id_str = "-1";
    int id_int = -1;
    boolean success = false;

    id_int = getId(id_int, success, return_in);

    Person person = Person.find(id_int);
    ArrayList<Book> borrowedBooks = person.borrowedBooks();

    if (!borrowedBooks.isEmpty()) {
      System.out.println("Your borrowed books are: ");
      for (Book borrowedBook : borrowedBooks) {
        System.out.println("ID: " + borrowedBook.id);
        System.out.println("Title: " + borrowedBook.title);
        System.out.println(" Author: " + borrowedBook.author);
        System.out.println("*".repeat(40));
      }

      System.out.println("Which book would you like to return (type the ID)?");
      System.out.print("> ");
      String returnID = return_in.nextLine();
      int returnID_int = -1;

      boolean converted = false;
      while (!converted) {
        try {
          returnID_int = Integer.parseInt(returnID);
          converted = true;
        } catch (Exception e) {
          System.out.println("That is not a valid ID. Please try again.");
        }
      }

      boolean returnSuccess = false;
      for (Book borrowedBook : borrowedBooks) {
        if (returnID_int == borrowedBook.id) {
          borrowedBook.returnBook();
          returnSuccess = true;
        }
      }

      if (!returnSuccess) {
        System.out.println("You didn't borrow that book. Please try again.");
      }

    } else {
      System.out.println("You have not borrowed any books.");
    }
    return_in.close();
  }
}

class Person {
  int id;
  String fname;
  String lname;

  public Person(String fname, String lname) {
    this.fname = fname;
    this.lname = lname;
  }

  public Person(String fname, String lname, int id) {
    this.fname = fname;
    this.lname = lname;
    this.id = id;
  }

  public void save() {
    try {
      Connection connection = DriverManager.getConnection("jdbc:postgresql:library");
      PreparedStatement stmt = connection
          .prepareStatement("INSERT INTO people (first_name, last_name) VALUES (?, ?)",
              Statement.RETURN_GENERATED_KEYS);

      stmt.setString(1, fname);
      stmt.setString(2, lname);

      int inserted = stmt.executeUpdate();

      if (inserted > 0) {
        ResultSet rs = stmt.getGeneratedKeys();
        if (rs.next()) {
          id = rs.getInt(1);
        }
        rs.close();
      }

      stmt.close();
      connection.close();
    } catch (SQLException ex) {
      ex.printStackTrace();
      System.exit(1);
    }
  }

  public void delete() {
    try {
      Connection connection = DriverManager.getConnection("jdbc:postgresql:library");
      PreparedStatement stmt = connection.prepareStatement("DELETE FROM people WHERE id = ?");

      if (id != 0) {
        stmt.setInt(1, id);
        stmt.executeUpdate();
        id = -1;
        fname = null;
        lname = null;
      }

      stmt.close();
      connection.close();
    } catch (SQLException ex) {
      ex.printStackTrace();
      System.exit(1);
    }
  }

  public static Person find(int id_num) {
    Person person = null;

    try {
      Connection connection = DriverManager.getConnection("jdbc:postgresql:library");
      PreparedStatement stmt = connection.prepareStatement("SELECT * FROM people WHERE id = ?");

      stmt.setInt(1, id_num);

      ResultSet rs = stmt.executeQuery();

      if (rs.next()) {
        person = new Person(rs.getString("first_name"), rs.getString("last_name"), id_num);
      }

      rs.close();
      stmt.close();
      connection.close();

    } catch (SQLException ex) {
      ex.printStackTrace();
      System.exit(1);
    }
    return person;
  }

  public ArrayList<Book> borrowedBooks() {
    ArrayList<Book> books = new ArrayList<Book>();

    try {
      Connection connection = DriverManager.getConnection("jdbc:postgresql:library");
      PreparedStatement stmt = connection.prepareStatement("SELECT * FROM books WHERE borrower = ?");

      stmt.setInt(1, id);

      ResultSet rs = stmt.executeQuery();

      while (rs.next()) {
        books.add(new Book(rs.getString("title"), rs.getString("author"), rs.getInt("id")));
      }
    } catch (SQLException ex) {
      ex.printStackTrace();
      System.exit(1);
    }

    return books;
  }
}

class Book {
  int id;
  String title;
  String author;
  int borrower;

  public Book(String title, String author) {
    this.title = title;
    this.author = author;

    try {
      Connection connection = DriverManager.getConnection("jdbc:postgresql:library");
      PreparedStatement stmt = connection
          .prepareStatement("SELECT id, borrower FROM books WHERE title = ? AND author = ?");

      stmt.setString(1, title);
      stmt.setString(2, author);

      ResultSet rs = stmt.executeQuery();

      if (rs.next()) {
        this.id = rs.getInt(1);
        this.borrower = rs.getInt(2);
      }

      rs.close();
      stmt.close();
      connection.close();
    } catch (SQLException ex) {
      ex.printStackTrace();
      System.exit(1);
    }
  }

  public Book(String title, String author, int id) {
    this.title = title;
    this.author = author;
    this.id = id;

    try {
      Connection connection = DriverManager.getConnection("jdbc:postgresql:library");
      PreparedStatement stmt = connection.prepareStatement("SELECT borrower FROM books WHERE id = ?");

      stmt.setInt(1, id);

      ResultSet rs = stmt.executeQuery();

      if (rs.next()) {
        this.borrower = rs.getInt(1);
      }
    } catch (SQLException ex) {
      ex.printStackTrace();
      System.exit(1);
    }
  }

  public void save() {
    try {
      Connection connection = DriverManager.getConnection("jdbc:postgresql:library");
      PreparedStatement test_stmt = connection
          .prepareStatement("SELECT COUNT(*) FROM books WHERE title = ? AND author = ?");

      test_stmt.setString(1, title);
      test_stmt.setString(2, author);

      ResultSet test_rs = test_stmt.executeQuery();

      if (test_rs.next()) {
        if (test_rs.getInt(1) > 0) {
          System.out.println("Book already exists!");
          return;
        }
      }

      test_rs.close();
      test_stmt.close();

      PreparedStatement stmt = connection.prepareStatement("INSERT INTO books (title, author) VALUES (?, ?)",
          Statement.RETURN_GENERATED_KEYS);

      stmt.setString(1, title);
      stmt.setString(2, author);

      int inserted = stmt.executeUpdate();

      if (inserted > 0) {
        ResultSet rs = stmt.getGeneratedKeys();
        if (rs.next()) {
          id = rs.getInt(1);
        }
        rs.close();
      }

      stmt.close();
      connection.close();

    } catch (SQLException ex) {
      ex.printStackTrace();
      System.exit(1);
    }

  }

  public void delete() {
    try {
      Connection connection = DriverManager.getConnection("jdbc:postgresql:library");
      PreparedStatement stmt = connection.prepareStatement("DELETE FROM people WHERE id = ?");

      if (id != 0) {
        stmt.setInt(1, id);
        stmt.executeUpdate();
        id = -1;
        title = null;
        author = null;
        borrower = -1;
      }

      stmt.close();
      connection.close();
    } catch (SQLException ex) {
      ex.printStackTrace();
      System.exit(1);
    }
  }

  public static Book find(int id_num) {
    Book book = null;

    try {
      Connection connection = DriverManager.getConnection("jdbc:postgresql:library");
      PreparedStatement stmt = connection.prepareStatement("SELECT * FROM books WHERE id = ?");

      stmt.setInt(1, id_num);

      ResultSet rs = stmt.executeQuery();

      if (rs.next()) {
        book = new Book(rs.getString("title"), rs.getString("author"), id_num);
      }

      rs.close();
      stmt.close();
      connection.close();

    } catch (SQLException ex) {
      ex.printStackTrace();
      System.exit(1);
    }
    return book;
  }

  public static ArrayList<Book> find(String title) {
    ArrayList<Book> books = new ArrayList<Book>();

    try {
      Connection connection = DriverManager.getConnection("jdbc:postgresql:library");
      PreparedStatement stmt = connection.prepareStatement("SELECT * FROM books WHERE title = ?");

      stmt.setString(1, title);

      ResultSet rs = stmt.executeQuery();

      while (rs.next()) {
        books.add(new Book(rs.getString("title"), rs.getString("author"), rs.getInt("id")));
      }

      rs.close();
      stmt.close();
      connection.close();

    } catch (SQLException ex) {
      ex.printStackTrace();
      System.exit(1);
    }
    return books;
  }

  public void borrowBook(int person_id) {
    borrower = person_id;

    try {
      Connection connection = DriverManager.getConnection("jdbc:postgresql:library");
      PreparedStatement stmt = connection.prepareStatement("UPDATE books SET borrower = ? WHERE id = ?");

      stmt.setInt(1, borrower);
      stmt.setInt(2, id);

      stmt.executeUpdate();

      stmt.close();
      connection.close();

    } catch (SQLException ex) {
      ex.printStackTrace();
      System.exit(1);
    }
  }

  public void returnBook() {
    borrower = -1;

    try {
      Connection connection = DriverManager.getConnection("jdbc:postgresql:library");
      PreparedStatement stmt = connection.prepareStatement("UPDATE books SET borrower = ? WHERE id = ?");

      stmt.setNull(1, 4);
      stmt.setInt(2, id);

      stmt.executeUpdate();

      stmt.close();
      connection.close();
    } catch (SQLException ex) {
      ex.printStackTrace();
      System.exit(1);
    }
  }
}
