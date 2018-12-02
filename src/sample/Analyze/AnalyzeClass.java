package sample.Analyze;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.*;

public class AnalyzeClass {

    private String[] ReservedWords = {"PROGRAM", "VAR", "REAL", "INTEGER", "BEGIN", "READ", "IF", "THEN", "GOTO", "ELSE",
            "FOR", "TO", "DO", "WRITE", "END"};

    private char[] Separators = {' ', ',', '(', ')', '[', ']', ';', ':', '+', '-', '*', '/', '<', '>'};

    private String outSource;
    private String tableCodesOfTokens;
    private Map<String, Integer> tableReservedWords = new LinkedHashMap<String, Integer>();
    private Map<String, Integer> tableIdentifiers = new LinkedHashMap<String, Integer>();
    private Map<String, Integer> tableConstants = new LinkedHashMap<String, Integer>();
    private Map<String, Integer> tableOperations = new LinkedHashMap<String, Integer>();
    private Map<String, Integer> tableDividers = new LinkedHashMap<String, Integer>();
    private Map<String, Integer> tableErrorTokens = new LinkedHashMap<String, Integer>();
    private LinkedList<HashMap<String, String>> listOfAllTokens = new LinkedList<HashMap<String, String>>();
    private HashMap<String, String> tmp;
    private ListIterator<HashMap<String, String>> itr;
    private String syntaxErrMessage;

    private ArrayList<String> vars = new ArrayList<String>();
    private ArrayList<String> markers = new ArrayList<String>();
    private ArrayList<String> init = new ArrayList<String>();
    private ArrayList<String> init2 = new ArrayList<String>();
    private boolean stillVars = true;
    private String semanticReport = "\nСемантический тест: [ERROR]";
    private boolean syntaxTestTrue = false;
    private boolean read = false;

    public String getSemanticReport() {
        if (syntaxTestTrue) {
            if (semanticReport.equals("\nСемантический тест: [ERROR]")) {
                return "\nСемантический тест: [ OK ]";
            }
            return semanticReport;
        }
        return "";
    }

    public void makeAnalyze(String source) throws IOException {
        outSource = "";
        tableCodesOfTokens = "";
        StringReader stringReader = new StringReader(source);
        BufferedReader bufferedReader = new BufferedReader(stringReader);
        String line;
        int numOfLine = 1;
        while ((line = bufferedReader.readLine()) != null) {
            processLine(line, numOfLine);
            numOfLine++;
        }
    }

    private void processLine(String line, int numOfLine) {
        StringBuilder token = new StringBuilder();
        boolean pointFound = false;
        String type = "constant";
        line += " ";
        for (int position = 0; position < line.length(); position++) {
            char symbol = line.charAt(position);
            if (Character.isLetterOrDigit(symbol) || symbol == '_') {
                if (!Character.isDigit(symbol) && !type.equals("error")) {
                    if (pointFound) type = "error";
                    else type = "word";
                }
                token.append(symbol);
            } else if (symbol == '.') {
                if (token.toString().toUpperCase().equals("END")) {
                    processToken("END", "word", numOfLine);
                    processToken(".", "divider", numOfLine);
                    token = new StringBuilder();
                    type = "constant";
                    pointFound = false;
                } else if (!token.toString().equals("") && type.equals("constant") && !pointFound) {
                    pointFound = true;
                    token.append(symbol);
                } else if (!token.toString().equals("")) {
                    type = "error";
                    token.append(symbol);
                }
            } else {
                for (int indexOfSeparator = 0; indexOfSeparator < Separators.length; indexOfSeparator++) {
                    if (Separators[indexOfSeparator] == symbol) {
                        if (!token.toString().equals("")) {
                            if (pointFound && line.charAt(position - 1) == '.' && type.equals("constant")) {
                                processToken(token.toString(), "error", numOfLine);
                            } else
                                processToken(token.toString(), type, numOfLine);
                        }
                        if (indexOfSeparator != 0) {
                            if (indexOfSeparator > 6) {
                                if (line.charAt(position + 1) == '=' && indexOfSeparator > 7) {
                                    processToken(symbol + "=", "operation", numOfLine);
                                    position++;
                                } else {
                                    if (symbol == ':' && line.charAt(position + 1) == '=') {
                                        processToken(":=", "operation", numOfLine);
                                        position++;
                                    } else if (symbol == ':')
                                        processToken(":", "divider", numOfLine);
                                    else if (symbol == '<' && line.charAt(position + 1) == '>') {
                                        processToken("<>", "operation", numOfLine);
                                        position++;
                                    } else processToken(String.valueOf(symbol), "operation", numOfLine);
                                }
                            } else processToken(String.valueOf(symbol), "divider", numOfLine);
                        }
                        token = new StringBuilder();
                        type = "constant";
                        pointFound = false;
                    }
                }
                if (symbol != ' ' && !token.toString().equals("")) {
                    type = "error";
                    token.append(symbol);
                }
            }
        }
        outSource += "\n";
    }

    private void processToken(String token, String type, int numOfLine) {
        HashMap<String, String> tokenToList;
        token = token.toUpperCase();
        int index;
        switch (type) {
            case "word":
                for (String word : ReservedWords) {
                    if (word.equals(token)) {
                        tokenToList = new HashMap<String, String>();
                        tokenToList.put("row", String.valueOf(numOfLine));
                        tokenToList.put("type", "res_word");
                        tokenToList.put("value", token);
                        listOfAllTokens.add(tokenToList);
                        if (tableReservedWords.containsKey(token)) {
                            outSource += "R" + tableReservedWords.get(token) + " ";
                            tableCodesOfTokens += numOfLine + "\tR" + tableReservedWords.get(token) + "\t" + token + "\n";
                            tokenToList.put("code", "R" + tableReservedWords.get(token));
                            return;
                        }
                        index = tableReservedWords.size() + 1;
                        tableReservedWords.put(token, index);
                        outSource += "R" + index + " ";
                        tableCodesOfTokens += numOfLine + "\tR" + index + "\t" + token + "\n";
                        tokenToList.put("code", "R" + index);
                        return;
                    }
                }
                if (Character.isDigit(token.charAt(0))) {
                    processToken(token, "error", numOfLine);
                    return;
                }
                tokenToList = new HashMap<String, String>();
                tokenToList.put("row", String.valueOf(numOfLine));
                tokenToList.put("type", "identifier");
                tokenToList.put("value", token);
                listOfAllTokens.add(tokenToList);
                if (tableIdentifiers.containsKey(token)) {
                    outSource += "I" + tableIdentifiers.get(token) + " ";
                    tableCodesOfTokens += numOfLine + "\tI" + tableIdentifiers.get(token) + "\t" + token + "\n";
                    tokenToList.put("code", "I" + tableIdentifiers.get(token));
                    return;
                }
                index = tableIdentifiers.size() + 1;
                tableIdentifiers.put(token, index);
                outSource += "I" + index + " ";
                tableCodesOfTokens += numOfLine + "\tI" + index + "\t" + token + "\n";
                tokenToList.put("code", "I" + index);
                return;
            case "constant":
                tokenToList = new HashMap<String, String>();
                tokenToList.put("row", String.valueOf(numOfLine));
                tokenToList.put("type", "constant");
                tokenToList.put("value", token);
                listOfAllTokens.add(tokenToList);
                if (tableConstants.containsKey(token)) {
                    outSource += "C" + tableConstants.get(token) + " ";
                    tableCodesOfTokens += numOfLine + "\tC" + tableConstants.get(token) + "\t" + token + "\n";
                    tokenToList.put("code", "C" + tableConstants.get(token));
                    return;
                }
                index = tableConstants.size() + 1;
                tableConstants.put(token, index);
                outSource += "C" + index + " ";
                tableCodesOfTokens += numOfLine + "\tC" + index + "\t" + token + "\n";
                tokenToList.put("code", "C" + index);
                return;
            case "operation":
                tokenToList = new HashMap<String, String>();
                tokenToList.put("row", String.valueOf(numOfLine));
                tokenToList.put("type", "operation");
                tokenToList.put("value", token);
                listOfAllTokens.add(tokenToList);
                if (tableOperations.containsKey(token)) {
                    outSource += "O" + tableOperations.get(token) + " ";
                    tableCodesOfTokens += numOfLine + "\tO" + tableOperations.get(token) + "\t" + token + "\n";
                    tokenToList.put("code", "O" + tableOperations.get(token));
                    return;
                }
                index = tableOperations.size() + 1;
                tableOperations.put(token, index);
                outSource += "O" + index + " ";
                tableCodesOfTokens += numOfLine + "\tO" + index + "\t" + token + "\n";
                tokenToList.put("code", "O" + index);
                return;
            case "divider":
                tokenToList = new HashMap<String, String>();
                tokenToList.put("row", String.valueOf(numOfLine));
                tokenToList.put("type", "divider");
                tokenToList.put("value", token);
                listOfAllTokens.add(tokenToList);
                if (tableDividers.containsKey(token)) {
                    outSource += "D" + tableDividers.get(token) + " ";
                    tableCodesOfTokens += numOfLine + "\tD" + tableDividers.get(token) + "\t" + token + "\n";
                    tokenToList.put("code", "D" + tableDividers.get(token));
                    return;
                }
                index = tableDividers.size() + 1;
                tableDividers.put(token, index);
                outSource += "D" + index + " ";
                tableCodesOfTokens += numOfLine + "\tD" + index + "\t" + token + "\n";
                tokenToList.put("code", "D" + index);
                return;
            case "error":
                tokenToList = new HashMap<String, String>();
                tokenToList.put("row", String.valueOf(numOfLine));
                tokenToList.put("type", "error");
                tokenToList.put("value", token);
                listOfAllTokens.add(tokenToList);
                if (tableErrorTokens.containsKey(token)) {
                    outSource += "E" + tableErrorTokens.get(token) + " ";
                    tableCodesOfTokens += numOfLine + "\tE" + tableErrorTokens.get(token) + "\t" + token + "\n";
                    tokenToList.put("code", "E" + tableErrorTokens.get(token));
                    return;
                }
                index = tableErrorTokens.size() + 1;
                tableErrorTokens.put(token, index);
                outSource += "E" + index + " ";
                tableCodesOfTokens += numOfLine + "\tE" + index + "\t" + token + "\n";
                tokenToList.put("code", "E" + index);
        }
    }

    public String getTable(String name) {
        StringBuilder dataFromTable = new StringBuilder();
        Map<String, Integer> tempTable = new LinkedHashMap<String, Integer>();
        char type = ' ';
        switch (name) {
            case "ReservedWords":
                tempTable = tableReservedWords;
                type = 'R';
                break;
            case "Identifiers":
                tempTable = tableIdentifiers;
                type = 'I';
                break;
            case "Constants":
                tempTable = tableConstants;
                type = 'C';
                break;
            case "Operations":
                tempTable = tableOperations;
                type = 'O';
                break;
            case "Dividers":
                tempTable = tableDividers;
                type = 'D';
                break;
            case "ErrorTokens":
                tempTable = tableErrorTokens;
                type = 'E';
                break;
        }
        for (Map.Entry<String, Integer> entry : tempTable.entrySet())
            dataFromTable.append(String.valueOf(type)).append(entry.getValue()).append("\t").append(entry.getKey()).append("\n");
        return dataFromTable.toString();
    }

    public String getOutSource() {
        return outSource;
    }

    public String syntaxTest(LinkedList<HashMap<String, String>> input) {
        tmp = new HashMap<>();
        itr = input.listIterator();

        if (itr.hasNext()) {
            tmp = itr.next();
        } else return "[Ошибка]\nПустая программа";

        if (!tmp.get("value").equals("PROGRAM")) {
            return "[Ошибка] строка " + tmp.get("row") + "\nОжидалось 'PROGRAM', получено '" + tmp.get("value") + "'";
        }

        if (itr.hasNext()) {
            tmp = itr.next();
        } else
            return "[Ошибка] строка " + tmp.get("row") + "\nПреждевременный конец программы";

        if (!tmp.get("type").equals("identifier")) {
            return "[Ошибка] строка " + tmp.get("row") + "\nНазвание программы '" + tmp.get("value") + "'не удовлетворяет требованиям";
        }

        if (itr.hasNext()) {
            tmp = itr.next();
        } else return "[Ошибка] строка " + tmp.get("row") + "\nПреждевременный конец программы";

        if (!tmp.get("value").equals(";")) {
            return "[Ошибка] строка " + tmp.get("row") + "\nОжидалось ';', получено '" + tmp.get("value") + "'";
        }

        if (itr.hasNext()) {
            tmp = itr.next();
        } else return "[Ошибка] строка " + tmp.get("row") + "\nПреждевременный конец программы";

        if (!tmp.get("value").equals("VAR")) {
            return "[Ошибка] строка " + tmp.get("row") + "\nОжидалось 'VAR', получено '" + tmp.get("value") + "'";
        }

        if (itr.hasNext()) {
            tmp = itr.next();
        } else return "[Ошибка] строка " + tmp.get("row") + "\nПреждевременный конец программы";

        if (!isVariables()) {
            vars.clear();
            return syntaxErrMessage;
        }
        stillVars = false;

        if (!tmp.get("value").equals("BEGIN")) {
            return "[Ошибка] строка " + tmp.get("row") + "\nОжидалось 'BEGIN', получено '" + tmp.get("value") + "'";
        }

        if (itr.hasNext()) {
            tmp = itr.next();
        } else return "321";

        if (!isBody()) {
            return syntaxErrMessage;
        }

        if (!tmp.get("value").equals("END")) {
            return "[Ошибка] строка " + tmp.get("row") + "\nОжидалось 'END', получено '" + tmp.get("value") + "'";
        }

        if (itr.hasNext()) {
            tmp = itr.next();
        } else return "[Ошибка] строка " + tmp.get("row") + "\nПреждевременный конец программы";

        if (!tmp.get("value").equals(".")) {
            return "[Ошибка] строка " + tmp.get("row") + "\nОжидалось '.', получено '" + tmp.get("value") + "'";
        }

        if (itr.hasNext()) {
            return "[Ошибка] строка " + tmp.get("row") + "\nОжидался конец программы";
        }
        syntaxTestTrue = true;
        return "Синтаксический тест:  [ OK ]";
    }

    private boolean isVariables() {
        boolean flag = false;
        do {
            if (!tmp.get("type").equals("identifier")) {
                break;
            }
            flag = true;
            if (!isVariablesDescription()) {
                return false;
            }

            if (itr.hasNext()) {
                tmp = itr.next();
            } else {
                syntaxErrMessage = "[Ошибка] строка " + tmp.get("row") + "\nПреждевременный конец программы";
                return false;
            }

            if (!tmp.get("value").equals(";")) {
                syntaxErrMessage = "[Ошибка] строка " + tmp.get("row") + "\nОжидалось ';'";
                ;
                return false;
            } else if (itr.hasNext()) {
                tmp = itr.next();
            } else {
                syntaxErrMessage = "[Ошибка] строка " + tmp.get("row") + "\nПреждевременный конец программы";
                return false;
            }
        } while (true);
        if (!flag) {
            syntaxErrMessage = "[Ошибка] строка " + tmp.get("row") + "\nПустое описание списка переменных";
            return false;
        }
        return true;
    }

    private boolean isVariablesDescription() {
        if (!isListOfNames()) {
            return false;
        }
        if (!tmp.get("value").equals(":")) {
            syntaxErrMessage = "[Ошибка] строка " + tmp.get("row") + "\nОжидалось ':'";
            return false;
        }
        if (itr.hasNext()) {
            tmp = itr.next();
        } else {
            syntaxErrMessage = "[Ошибка] строка " + tmp.get("row") + "\nПреждевременный конец программы";
            return false;
        }
        if (!tmp.get("type").equals("res_word")) {
            syntaxErrMessage = "[Ошибка] строка " + tmp.get("row") + "\n'" + tmp.get("value") + "' не является типом переменных";
            return false;
        }
        return true;
    }

    private boolean isListOfNames() {
        init2.clear();
        if (!tmp.get("type").equals("identifier")) {
            syntaxErrMessage = "[Ошибка] строка " + tmp.get("row") + "\n'" + tmp.get("value") + "' не является идентификатором";
            return false;
        }
        if (!init.contains(tmp.get("value"))) {
            init2.add(tmp.get("value"));
        }
        if (stillVars) vars.add(tmp.get("value"));
        if (!vars.contains(tmp.get("value"))) {
            semanticReport += "\n\t cтрока " + tmp.get("row") + ": переменная '" + tmp.get("value") + "' не объявлена!";
        }
        if (!read && !init.contains(tmp.get("value")) && !stillVars) {
            semanticReport += "\n\t cтрока " + tmp.get("row") + ": переменная '" + tmp.get("value") + "' не инициализирована!";
        }
        if (itr.hasNext()) {
            tmp = itr.next();
        } else {
            syntaxErrMessage = "[Ошибка] строка " + tmp.get("row") + "\nПреждевременный конец программы";
            return false;
        }
        do {
            if (!tmp.get("value").equals(",")) {
                break;
            }
            if (itr.hasNext()) {
                tmp = itr.next();
            } else {
                syntaxErrMessage = "[Ошибка] строка " + tmp.get("row") + "\nПреждевременный конец программы";
                return false;
            }
            if (!tmp.get("type").equals("identifier")) {
                syntaxErrMessage = "[Ошибка] строка " + tmp.get("row") + "\n'" + tmp.get("value") + "' не является идентификатором";
                return false;
            }
            if (!init.contains(tmp.get("value"))) {
                init2.add(tmp.get("value"));
            }
            if (stillVars) vars.add(tmp.get("value"));
            if (!vars.contains(tmp.get("value"))) {
                semanticReport += "\n\t cтрока " + tmp.get("row") + ": переменная '" + tmp.get("value") + "' не объявлена!";
            }
            if (!read && !init.contains(tmp.get("value")) && !stillVars) {
                semanticReport += "\n\t cтрока " + tmp.get("row") + ": переменная '" + tmp.get("value") + "' не инициализирована!";
            }
            if (itr.hasNext()) {
                tmp = itr.next();
            } else {
                syntaxErrMessage = "[Ошибка] строка " + tmp.get("row") + "\nПреждевременный конец программы";
                return false;
            }
        } while (true);
        if (!stillVars && read) {
            init.addAll(init2);
        }
        return true;
    }

    private boolean isBody() {
        while (true) {
            if (!isOperator()) {
                if (!tmp.get("value").equals("END")) {
                    return false;
                }
                break;
            }
            if (tmp.get("value").equals(";")) {
                if (itr.hasNext()) {
                    tmp = itr.next();
                } else {
                    syntaxErrMessage = "[Ошибка] строка " + tmp.get("row") + "\nПреждевременный конец программы";
                    return false;
                }
                if (tmp.get("value").equals("END")) {
                    syntaxErrMessage = "[Ошибка] строка " + tmp.get("row") + "\n';' перед END";
                    return false;
                }
            } else {
                if (!tmp.get("value").equals("END")) {
                    syntaxErrMessage = "[Ошибка] строка " + tmp.get("row") + "\nОжидалось ';'";
                    return false;
                }
            }
        }
        return true;
    }

    private boolean isOperator() {
        if (!isMarker()) {
            if (!isUnmarkOperator()) {
                return false;
            }
        } else {
            if (markers.contains(tmp.get("value"))) {
                semanticReport += "\n\t cтрока " + tmp.get("row") + ": многократное повторение метки '" + tmp.get("value") + "'";
            } else {
                markers.add(tmp.get("value"));
            }
            if (itr.hasNext()) {
                tmp = itr.next();
            } else {
                syntaxErrMessage = "[Ошибка] строка " + tmp.get("row") + "\nПреждевременный конец программы";
                return false;
            }
            if (tmp.get("value").equals(":")) {
                if (itr.hasNext()) {
                    tmp = itr.next();
                } else {
                    syntaxErrMessage = "[Ошибка] строка " + tmp.get("row") + "\nПреждевременный конец программы";
                    return false;
                }
                if (!isUnmarkOperator()) {
                    return false;
                }
            } else {
                syntaxErrMessage = "[Ошибка] строка " + tmp.get("row") + "\nОжидалось ':', получено '" + tmp.get("value") + "'";
                return false;
            }
        }
        return true;
    }

    private boolean isMarker() {
        if (!tmp.get("type").equals("constant")) {
            return false;
        }
        return true;
    }

    private boolean isUnmarkOperator() {
        HashMap<String, String> localTmp = tmp;
        ListIterator<HashMap<String, String>> localItr = itr;
        if (isIf()) {
            return true;
        } else {
            tmp = localTmp;
            itr = localItr;
        }
        if (isGoTo()) {
            return true;
        } else {
            tmp = localTmp;
            itr = localItr;
        }
        if (isRead()) {
            return true;
        } else {
            tmp = localTmp;
            itr = localItr;
        }
        if (isWrite()) {
            return true;
        } else {
            tmp = localTmp;
            itr = localItr;
        }
        if (isForDo()) {
            return true;
        } else {
            tmp = localTmp;
            itr = localItr;
        }
        if (isAssign()) {
            return true;
        } else {
            tmp = localTmp;
            itr = localItr;
        }
        syntaxErrMessage = "[Ошибка] строка " + tmp.get("row") + "\nОшибка синтаксиса оператора";
        return false;
    }

    private boolean isIf() {
        if (!tmp.get("value").equals("IF")) {
            return false;
        }
        if (itr.hasNext()) {
            tmp = itr.next();
        } else {
            syntaxErrMessage = "конец программы";
            return false;
        }
        if (!isException()) {
            return false;
        }
        if (!tmp.get("value").equals("THEN")) {
            return false;
        }
        if (itr.hasNext()) {
            tmp = itr.next();
        } else {
            syntaxErrMessage = "конец программы";
            return false;
        }
        if (!isForDoBody()) {
            return false;
        }
        if (tmp.get("value").equals("ELSE")) {
            if (itr.hasNext()) {
                tmp = itr.next();
            } else {
                syntaxErrMessage = "конец программы";
                return false;
            }
            if (!isForDoBody()) {
                return false;
            }
        }
        return true;
    }

    private boolean isRead() {
        if (!tmp.get("value").equals("READ")) {
            return false;
        }
        if (itr.hasNext()) {
            tmp = itr.next();
        } else {
            syntaxErrMessage = "конец программы";
            return false;
        }
        if (!tmp.get("value").equals("(")) {
            return false;
        }
        if (itr.hasNext()) {
            tmp = itr.next();
        } else {
            syntaxErrMessage = "конец программы";
            return false;
        }
        read = true;
        if (!isListOfNames()) {
            read = false;
            return false;
        }
        read = false;
        if (!tmp.get("value").equals(")")) {
            return false;
        }
        if (itr.hasNext()) {
            tmp = itr.next();
        } else {
            syntaxErrMessage = "конец программы";
            return false;
        }
        return true;
    }

    private boolean isWrite() {
        if (!tmp.get("value").equals("WRITE")) {
            return false;
        }
        if (itr.hasNext()) {
            tmp = itr.next();
        } else {
            syntaxErrMessage = "конец программы";
            return false;
        }
        if (!tmp.get("value").equals("(")) {
            return false;
        }
        if (itr.hasNext()) {
            tmp = itr.next();
        } else {
            syntaxErrMessage = "конец программы";
            return false;
        }
        if (!isListOfNames()) {
            return false;
        }
        if (!tmp.get("value").equals(")")) {
            return false;
        }
        if (itr.hasNext()) {
            tmp = itr.next();
        } else {
            syntaxErrMessage = "конец программы";
            return false;
        }
        return true;
    }

    private boolean isGoTo() {
        if (!tmp.get("value").equals("GOTO")) {
            return false;
        }
        if (itr.hasNext()) {
            tmp = itr.next();
        } else {
            syntaxErrMessage = "конец программы";
            return false;
        }
        if (!isMarker()) {
            return false;
        }
        if (!markers.contains(tmp.get("value")))
            semanticReport += "\n\t cтрока " + tmp.get("row") + ": метка перехода '" + tmp.get("value") + "' не обявлена";
        if (itr.hasNext()) {
            tmp = itr.next();
        } else {
            syntaxErrMessage = "конец программы";
            return false;
        }
        return true;
    }

    private boolean isAssign() {
        if (!tmp.get("type").equals("identifier")) {
            return false;
        }
        String in = tmp.get("value");
        if (!vars.contains(tmp.get("value"))) {
            semanticReport += "\n\t cтрока " + tmp.get("row") + ": переменная '" + tmp.get("value") + "' не объявлена!";
        }
        if (itr.hasNext()) {
            tmp = itr.next();
        } else {
            syntaxErrMessage = "конец программы";
            return false;
        }
        if (!tmp.get("value").equals(":=")) {
            return false;
        }
        if (itr.hasNext()) {
            tmp = itr.next();
        } else {
            syntaxErrMessage = "конец программы";
            return false;
        }
        if (!isException()) {
            return false;
        }
        if (!init.contains(in)) {
            init.add(in);
        }
        return true;
    }

    private boolean isException() {
        if (!simpleException()) {
            return false;
        }
        if (!tmp.get("type").equals("operation")) {
            return true;
        }
        if (!simpleException()) {
            return false;
        }
        return true;
    }

    private boolean isMultiplier() {
        if (!tmp.get("type").equals("identifier") && !tmp.get("type").equals("constant")) {
            if (!tmp.get("value").equals("(")) {
                return false;
            }
            if (itr.hasNext()) {
                tmp = itr.next();
            } else {
                syntaxErrMessage = "конец программы";
                return false;
            }
            if (!simpleException()) {
                return false;
            }
            if (!tmp.get("value").equals(")")) {
                return false;
            }
        }
        if (tmp.get("type").equals("identifier")) {
            if (!vars.contains(tmp.get("value"))) {
                semanticReport += "\n\t cтрока " + tmp.get("row") + ": переменная '" + tmp.get("value") + "' не объявлена!";
            }
            if (!init.contains(tmp.get("value"))) {
                semanticReport += "\n\t cтрока " + tmp.get("row") + ": переменная '" + tmp.get("value") + "' не инициализирована!";
            }
        }
        return true;
    }

    private boolean isTerm() {
        if (!isMultiplier()) {
            return false;
        }
        if (itr.hasNext()) {
            tmp = itr.next();
        } else {
            syntaxErrMessage = "конец программы";
            return false;
        }
        do {
            if (!tmp.get("value").equals("/") || !tmp.get("value").equals("*")) {
                break;
            }
            if (itr.hasNext()) {
                tmp = itr.next();
            } else {
                syntaxErrMessage = "конец программы";
                return false;
            }
            if (!isMultiplier()) {
                return false;
            }
        } while (true);
        return true;
    }

    private boolean simpleException() {
        if (!isTerm()) {
            return false;
        }
        while (tmp.get("type").equals("operation")) {
            if (itr.hasNext()) {
                tmp = itr.next();
            } else {
                syntaxErrMessage = "конец программы";
                return false;
            }
            if (!isTerm()) {
                return false;
            }
        }
        return true;
    }


    private boolean isForDo() {
        if (!tmp.get("value").equals("FOR")) {
            return false;
        }
        if (itr.hasNext()) {
            tmp = itr.next();
        } else {
            syntaxErrMessage = "конец программы";
            return false;
        }
        if (!isIndexException()) {
            return false;
        }
        if (!tmp.get("value").equals("DO")) {
            return false;
        }
        if (itr.hasNext()) {
            tmp = itr.next();
        } else {
            syntaxErrMessage = "конец программы";
            return false;
        }
        if (!isForDoBody()) {
            return false;
        }
        return true;
    }

    private boolean isIndexException() {
        boolean flagtmp = false;
        if (!tmp.get("type").equals("identifier")) {
            return false;
        }
        String in = tmp.get("value");
        if (!init.contains(in)) {
            flagtmp = true;
            init.add(in);
        }
        if (!vars.contains(tmp.get("value"))) {
            semanticReport += "\n\t cтрока " + tmp.get("row") + ": переменная '" + tmp.get("value") + "' не объявлена!";
        }
        if (itr.hasNext()) {
            tmp = itr.next();
        } else {
            syntaxErrMessage = "конец программы";
            if (flagtmp) init.remove(in);
            return false;
        }
        if (!tmp.get("value").equals(":=")) {
            if (flagtmp) init.remove(in);
            return false;
        }
        if (itr.hasNext()) {
            tmp = itr.next();
        } else {
            syntaxErrMessage = "конец программы";
            if (flagtmp) init.remove(in);
            return false;
        }
        if (!isException()) {
            if (flagtmp) init.remove(in);
            return false;
        }
        if (!tmp.get("value").equals("TO")) {
            if (flagtmp) init.remove(in);
            return false;
        }
        if (itr.hasNext()) {
            tmp = itr.next();
        } else {
            syntaxErrMessage = "конец программы";
            if (flagtmp) init.remove(in);
            return false;
        }
        if (!isException()) {
            if (flagtmp) init.remove(in);
            return false;
        }
        return true;
    }

    private boolean isForDoBody() {
        if (!isOperator()) {
            if (!tmp.get("value").equals("BEGIN")) {
                return false;
            }
            if (itr.hasNext()) {
                tmp = itr.next();
            } else {
                syntaxErrMessage = "конец программы";
                return false;
            }
            if (!isBody()) {
                return false;
            }
            if (!tmp.get("value").equals("END")) {
                return false;
            }
            if (itr.hasNext()) {
                tmp = itr.next();
            } else {
                syntaxErrMessage = "конец программы";
                return false;
            }
        }
        return true;
    }

    public LinkedList<HashMap<String, String>> getListOfAllTokens() {
        return listOfAllTokens;
    }

    public String printListOfAllTokens() {
        String res = "";
        for (HashMap<String, String> temp : listOfAllTokens) {
            res += temp.get("row") + "\t" + temp.get("type") + "\t" + temp.get("code") + "\t" + temp.get("value") + "\n";
        }
        return res;
    }
}
