# TODO List

## 🔧 Текущие задачи
- Нужно сделать, чтобы порядок слов в таком варианте: word1/word2 не имел значения

---

## 🔴 Критические баги (могут сломать бота)

- **[BUG] NPE при получении callback** — `WordTrainerBot.java:49`, метод `onUpdateReceived()`.
  `update.getCallbackQuery().getMessage()` может вернуть `null` в некоторых типах callback-ов.
  Добавить null-check перед обращением к `getMessage()`.

- **[BUG] IndexOutOfBoundsException при выборе словаря** — `WordTrainerBot.java:117`.
  `Integer.parseInt(data.substring(5))` без проверки границ. Если придёт `dict:999` — бот упадёт.
  Добавить try-catch + проверку `index < dictionaryManager.size()`.

- **[BUG] IndexOutOfBoundsException в DictionaryManager** — `DictionaryManager.java:28`, метод `loadDictionaryByIndex()`.
  Нет проверки границ перед `names.get(index)`. Добавить валидацию индекса.

- **[BUG] NPE если trainer == null** — `WordTrainerBot.java:157`.
  `trainer.handleAnswer(text)` вызывается без проверки на `null`.
  Добавить проверку `if (trainer == null)` перед вызовом.

- **[BUG] Сессии никогда не удаляются** — `WordTrainerBot.java:27`.
  `Map<Long, UserSession> sessions` растёт бесконечно. При большом числе пользователей — утечка памяти.
  Реализовать очистку сессий старше N часов (например, через scheduled task).

---

## 🟠 Проблемы производительности

- **[PERF] O(n²) алгоритм в resetWithWrongOnly()** — `AbstractWordTrainer.java:65`.
  `correctIndices` объявлен как `List<Integer>`, вызов `contains()` в цикле даёт O(n²).
  Заменить на `Set<Integer> correctIndices` для O(1) поиска.

- **[PERF] Regex компилируется при каждом вызове** — `WordUtil.java:16`.
  `replaceAll("[()]", "")` перекомпилирует regex каждый раз.
  Вынести в `static final Pattern PARENS = Pattern.compile("[()]");`.

---

## 🟡 Качество кода

- **[REFACTOR] Парсинг файлов сломается если в тексте есть "="** — `FileUtil.java:34,46`.
  `line.split("=")` разобьёт строку на 4+ частей если в описании слова встречается `=`.
  Заменить на `line.split("=", 3)` (для слов) и `line.split("=", 2)` (для dictionaries.txt).

- **[REFACTOR] Magic strings по всему коду** — строки `"dict:"`, `"mode:"`, `"retry_wrong"`,
  `"back_to_menu"`, `"retry_wrong"` и эмодзи разбросаны по `WordTrainerBot.java`.
  Вынести в отдельный класс-константы `BotCallback` или `BotMessages`.

- **[REFACTOR] Дублирование кода при создании кнопок** — `WordTrainerBot.java:208-222`.
  Паттерн создания `InlineKeyboardButton` повторяется многократно.
  Вынести в helper-метод `createButton(String text, String callback)`.

- **[REFACTOR] Подключённый SLF4J не используется** — `pom.xml` содержит зависимость на SLF4J,
  но везде используется `System.out` / `System.err`.
  Заменить все `System.out.println` / `System.err.println` на `log.info` / `log.error`.

- **[REFACTOR] Неполный стек ошибки в Main.java** — `Main.java:26`.
  `System.err.println(e.getMessage())` теряет стек. Заменить на `log.error("...", e)`.

---

## 🟢 Новые функции

- **[FEATURE] Сохранение прогресса пользователя** — сейчас весь прогресс сбрасывается при
  перезапуске бота. Добавить сохранение в JSON-файл или SQLite: какие слова изучены,
  сколько раз ответил правильно/неправильно.

- **[FEATURE] Показывать описание слова при неправильном ответе** — сейчас бот только говорит
  "Неправильно". Добавить в ответ правильный вариант + описание (englishDescription).

- **[FEATURE] Spaced repetition** — слова, на которые пользователь чаще ошибается,
  показывать чаще. Реализовать простой алгоритм весов на основе статистики ответов.

- **[FEATURE] Добавить unit-тесты** — нет ни одного теста. Минимальное покрытие:
  `WordUtil` (нормализация, сравнение), `FileUtil` (парсинг файлов), `AbstractWordTrainer`
  (логика shuffledIndices, resetWithWrongOnly, existsMoreWords).
  Добавить JUnit 5 + Mockito в pom.xml.