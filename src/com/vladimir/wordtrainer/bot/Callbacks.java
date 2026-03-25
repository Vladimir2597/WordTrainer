package com.vladimir.wordtrainer.bot;

public enum Callbacks {
    DICT_PREFIX("dict:", null),
    MODE_DEFINITION("mode_definition", "По определению на английском"),
    MODE_RUSSIAN("mode_russian", "По слову на русском"),
    MODE_SENTENCE("mode_sentence", "✍️ Составить предложение"),
    RETRY_WRONG("retry_wrong", "Повторить неправильные"),
    RETRY_ALL("retry_all", "Повторить всё заново"),
    BACK_TO_MENU("back_to_menu", "Выбрать другой словарь"),
    LISTEN_PREFIX("listen:", "🔊 Произнести слово"),
    MANAGE_DICTIONARIES("manage_dictionaries", "⚙️ Управление словарями"),
    ADD_EXISTING_DICTIONARY("add_existing_dictionary", "➕ Добавить словарь к себе"),
    ADD_DICT_PREFIX("add_dict:", null),
    REMOVE_DICTIONARY("remove_dictionary", "➖ Убрать из моего списка"),
    REMOVE_DICT_PREFIX("remove_dict:", null),
    UPLOAD_DICTIONARY("upload_dictionary", "📥 Добавить новый словарь"),
    DELETE_DICTIONARY("delete_dictionary", "🗑️ Удалить словарь полностью"),
    DELETE_DICT_PREFIX("delete_dict:", null),
    DICT_VISIBILITY_PUBLIC("dict_visibility_public", "🌍 Публичный"),
    DICT_VISIBILITY_PRIVATE("dict_visibility_private", "🔒 Только для меня");

    private final String callback;
    private final String buttonText;

    Callbacks(String callback, String buttonText) {
        this.callback = callback;
        this.buttonText = buttonText;
    }

    public String callback() { return callback; }
    public String buttonText() { return buttonText; }
}