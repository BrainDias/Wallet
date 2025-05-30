Часть невалидных JSONов обрабатывается спрингом без дополнительных настроек и на
запросы, в которых значения примитивных полей, строк, UUID не соответствуют
нужному формату, выбрасываются исключения такого формата:
```JSON
{
    "timestamp": "2025-03-06T12:00:00.123",
    "status": 400,
    "error": "Bad Request",
    "message": "JSON parse error: Cannot deserialize value of type `int` from String \"abc\""
}
```

Для случаев null-полей, а также отрицательного amount в JSONе (должно быть положительным по
логике запросов), применяются аннотации валидации из spring-validation, которые выдают в
ответе сообщение о том, какое именно поле было указано неправильно.

Для случаев, когда баланс недостаточен или указанного кошелька не существует, выбрасываются
исключения ResponseStatusException, которые возвращают статус 404 NOT FOUND в случае
несуществующего кошелька, и 400 BAD REQUEST - в случае недостаточного баланса, а также
соответствующие сообщения.

Исходя из небольшого количества исключений, которые должны быть обработаны вручную и
простой логики их обработки (не нужно восстанавливать состояния или формировать
развернутые ответы), я считаю излишним написание ExceptionHandler'a.

Тестирование под нагрузкой проводилось с помощью утилиты k6. Ее отчет представлен ниже, а
скрипт "load_test.js", применявшийся для тестирования, находится в корневой папке проекта.

```execution: local
        script: /mnt/c/Users/MrKomp/Downloads/Telegram Desktop/load_test.js
        output: -

     scenarios: (100.00%) 1 scenario, 3000 max VUs, 31s max duration (incl. graceful stop):
              * constant_rps: 1000.00 iterations/s for 1s (maxVUs: 500-3000, gracefulStop: 30s)


     data_received..................: 54 kB  11 kB/s
     data_sent......................: 176 kB 35 kB/s
     dropped_iterations.............: 0    0/s
     http_req_blocked...............: avg=2.39ms   min=2.49µs  med=311.6µs  max=79.12ms p(90)=6.2ms    p(95)=13.37ms
     http_req_connecting............: avg=2.12ms   min=0s      med=167.64µs max=79.08ms p(90)=5.99ms   p(95)=11.43ms
     http_req_duration..............: avg=2.21s    min=30.51ms med=2.28s    max=4.62s   p(90)=3.74s    p(95)=4s
       { expected_response:true }...: avg=2.21s    min=30.51ms med=2.28s    max=4.62s   p(90)=3.74s    p(95)=4s
     http_req_failed................: 0.00%  0 out of 745
     http_req_receiving.............: avg=112.36µs min=10.79µs med=86.57µs  max=2.32ms  p(90)=155.01µs p(95)=207.08µs
     http_req_sending...............: avg=344.32µs min=8.39µs  med=50.08µs  max=9.9ms   p(90)=565.47µs p(95)=3.41ms
     http_req_tls_handshaking.......: avg=0s       min=0s      med=0s       max=0s      p(90)=0s       p(95)=0s
     http_req_waiting...............: avg=2.21s    min=30.27ms med=2.28s    max=4.62s   p(90)=3.74s    p(95)=4s
     http_reqs......................: 1001    199.912223/s
     iteration_duration.............: avg=2.21s    min=32.8ms  med=2.29s    max=4.62s   p(90)=3.74s    p(95)=4s
     iterations.....................: 1001    199.912223/s
     vus............................: 21     min=21       max=589
     vus_max........................: 628    min=591      max=628
```

Для того, чтобы уменьшить количество откатов транзакций, которые могут привести к 5ХХ ошибкам, можно настроить
несколько параметров для подключения к БД со стороны приложения и параметров самой БД. Они настроены через
переменные окружения в compose.yaml

Изменение параметров конфигурации (если они имелись в виду) и приложения, и БД без перезапуска контейнеров
возможно через переменные окружения с помощью Kubernetes ConfigMap, а также работающего сервиса Zookeeper.
В самом же простом случае, в новых версиях docker, переменные окружения можно менять командой docker update,
по одному параметру одной командой.

Все кейсы работы сервиса и контроллера были покрыты юнит-тестами, а также был написан интеграционный тест сервиса
с контейнером PostgreSQL с помощью Test Containers.