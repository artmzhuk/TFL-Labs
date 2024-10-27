
## API Эндпоинты

### `/start` - Инициализация DFA

- **Метод:** `POST`
- **Тело запроса:** `json`
  ```json
  {
    "maxSize": 100,
    "nesting": 3
  }
  ```
    - `maxSize` (int): Максимальное количество состояний.
    - `nesting` (int): Уровень вложенности для генерации лексем.

- **Ответ:** `200`

### `/checkWord` - Проверка вхождения слова

- **Метод:** `POST`
- **Тело запроса:** `json`
  ```json
  {
    "word": "exampleWord"
  }
  ```

- **Ответ:** `200 json`
  ```json
  {
    "response": "1" // 1 если слово принимается, 0 если нет
  }
  ```


### `/checkTable` - Проверка структуры DFA

- **Метод:** `POST`
- **Тело запроса:** `json`
  ```json
  {
    "main_prefixes": "a b c",
    "non_main_prefixes": "d e",
    "suffixes": "x y z",
    "table": "1 0"
  }
  ```

- **Ответ:** `200 json`
  ```json
  {
    "response": "Success",
    "type": true
  }
  ```



