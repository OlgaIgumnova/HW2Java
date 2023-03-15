// Дана строка (получение через обычный текстовый файл!!!)

// "фамилия":"Иванов","оценка":"5","предмет":"Математика"
// "фамилия":"Петрова","оценка":"4","предмет":"Информатика"

// Написать метод(ы), который распарсит строку и, используя StringBuilder, создаст строки вида:
// Студент [фамилия] получил [оценка] по предмету [предмет].


import java.io.FileReader;
import java.io.IOException;
import java.lang.ProcessBuilder.Redirect.Type;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Scanner;

public class HW2 {
    public static void main(String[] args) {
        // Чтение json строки из файла
        String stringJson = readFile("student.json");
        // Создание массива объектов класса Students, поля которого повторяют структуру
        // записей в json строке
        Students[] students = new Students[getLines(stringJson)];
        for (int i = 0; i < students.length; i++) {
            students[i] = new Students();
        }
        // Парсинг json строки в массив объектов
        students = parseJson(stringJson, students);
        // Формирование последовательности строк требуемого формата
        String listStudents = createListStudents(students);
        System.out.println(listStudents);
    }

    static int getLines(String string) {
        return (int) string.chars().filter(ch -> ch == '}').count();
    }

    static Students[] parseJson(String stringJson, Students[] students) {
        // Метод дробящий исходную строку на подстроки, содержащие структурированные
        // записи данных
        int startPosition = 0, lenLine;
        int i = 0;
        do {
            lenLine = stringJson.indexOf("}", startPosition);
            students[i] = (lenLine > 0)
                    ? parseString(students[i], stringJson.substring(startPosition, lenLine))
                    : parseString(students[i], stringJson.substring(startPosition));
            startPosition = lenLine + 1;
        } while (lenLine > 0 && ++i < students.length);
        return students;
    }

    static Students parseString(Students student, String stringValues) {
        // Разбор строки на элементарные компоненты words[] и запись их в массив
        // Students[]
        String[] words = stringValues.split("\"");
        // Выделение пар данных <перемнная>:<значение> из блоков по 4 элемента массива
        if ((words.length - 1) / 4 > 0)
            for (int i = 1; i < words.length; i += 4)
                if (!words[i].equals("null") && !words[i + 2].equals("null"))
                    // запись в объект класса Students
                    student.setFieldNameRu(student, words[i], words[i + 2]);
        return student;
    }

    static String createListStudents(Students[] students) {
        // Создание списка студентов из полей массива объектов класса Students
        StringBuilder builder = new StringBuilder();
        for (Students student : students) {
            // Формирование списка на основании данных массива Students[]
            builder.append(student.getAllData(student)).append("\n");
        }
        if (builder.length() > 0) // Фиксим последний пробел
            builder.deleteCharAt(builder.length() - 1);
        return builder.toString();
    }

    static String readFile(String nameFile) {
        // Чтение данных из файла в переменную типа String
        String text = new String();
        try (FileReader fr = new FileReader(nameFile)) {
            Scanner scanner = new Scanner(fr);
            if (scanner.hasNext())
                text = scanner.nextLine();
            System.out.println("Данные считаны!");
            fr.close();
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
            return text;
        }
        return text;
    }
}

class Students {
    // Класс, поля которого повторяют структуру json документа
    public String surName;
    public int grade;
    public String subject;
    static String[] specificate[] = { { "surName", "фамилия", "Студент" }, { "grade", "оценка", "получил" },
            { "subject", "предмет", "по предмету" } };

    public void setFieldNameRu(Students student, String name, String value) {
        // метод записи данных в поле, соответствующее русскому названию колонки
        // значений
        String nameField = getFieldName(name);
        if (nameField == null)
            System.out.println(String.format("Поле, соответствующее имени \"%s\" не найдено", name));
        else if (!value.isEmpty()) {
            student.setFieldName(student, nameField, value);
        }

    }

    public void setFieldName(Students student, String name, String value) {
        // метод записи данных в поле, по имени поля
        try {
            Field field = Students.class.getField(name);
            Object obj = field.getGenericType();
            if (obj.equals(int.class))
                field.set(student, (value.equals(null)) ? null : Integer.parseInt(value)); // строка, вынесшая мозг
            else if (obj.equals(String.class))
                field.set(student, value);
            else
                System.out.println("Требуется обработка типа " + obj);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            System.out.println("И чего ему ... не хватает");
        }
    }

    public static String getFieldName(String name) {
        // Возвращает имя поля по русскому названию
        String fieldName = null;
        for (String[] strings : specificate)
            if (strings[1].equals(name))
                fieldName = strings[0];
        return fieldName;
    }

    public static String getAllData(Students student) {
        // Вывод всех динамических полей в соответствии с шаблоном
        StringBuilder builder = new StringBuilder();
        Field[] fields = Students.class.getDeclaredFields();
        int i = 0;
        for (Field field : fields) {
            String f = Modifier.toString(field.getModifiers());
            if (f.indexOf("static") < 0) // // field.getModifiers())!=8
                try {
                    builder.append(specificate[i++][2]).append(" ").append(field.get(student)).append(" ");
                } catch (Exception e) {
                    e.getMessage();
                }
        }
        if (builder.length() > 0)
            builder.deleteCharAt(builder.length() - 1);
        return builder.toString();
    }
}