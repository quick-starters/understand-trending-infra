## Apache Airflow



데이터 엔지니어링에서 데이터 ETL(Extract, Transform, Load) 과정을 통해 데이터를 가공하며 적재한다. 

이러한 ETL 과정들은 여러개의 Sequential한 로직들이 존재(output이 다음 작업의 input이 되는)하는데 이런 로직들을 한번에 관리해야할 필요가 생김.



### Apache Airflow의 장점

- Apache Airflow는 Python 기반으로 만들어졌기 때문에, 데이터 분석을 하는 분들도 쉽게 코드를 작성할 수 있음
- Airflow 콘솔이 따로 존재해 Task 관리를 서버에서 들어가 관리하지 않아도 되고, 각 작업별 시간이 나오기 때문에 bottleneck을 찾을 때에도 유용함
- 또한 구글 클라우드 플랫폼(BigQuery, Dataflow)을 쉽게 사용할 수 있도록 제공되기 때문에 GCP를 사용하시면 반드시 사용할 것을 추천함
  - Google Cloud Platform에는 Managed Airflow인 [Google Cloud Composer](https://cloud.google.com/composer/?hl=ko)가 있음
  - 직접 환경을 구축할 여건이 되지 않는다면 이런 서비스를 사용하는 것을 추천 :)





## 설치 & 실행

- pip로 손쉽게 설치 가능

1. `pip install apache-airflow `

2. `airflow db init` -> airflow를 위한 db를 초기화

3. ```
   airflow users create \
    --username admin \
    --firstname FIRST_NAME \
    --lastname LAST_NAME \
    --role Admin \
    --password admin \
    --email pbj00812@gmail.com
   ```

   계정 생성

4. `airflow webserver -p 8090`

5. webUi에 접속.





## DAG

- DAG는 Directed Acyclic Graph의 약자로 Airflow에선 workflow라고 설명함
  - Task의 집합체

- Schedule은 예정된 스케쥴로 cron 스케쥴의 형태와 동일하게 사용
- Owner는 소유자를 뜻하는 것으로 생성한 유저를 뜻함
- Recent Tasks/DAG Runs에 최근 실행된 Task들이 나타나며, 실행 완료된 것은 초록색, 재시도는 노란색, 실패는 빨간색으로 표시됨
- `~/airflow/dags` 폴더에 dags를 정의한다.



## Task

- Task는 **Operator, Sensor, Taskflow**의 세가지가 있음.

- Operator:  Bash, Python 등과 연결되어 있는 미리 정의된 작업 템플릿

  - BashOperator : bash command를 실행
  - PythonOperator : Python 함수를 실행
  - EmailOperator : Email을 발송
  - MySqlOperator : sql 쿼리를 수행

- Sensor: Sensor는 시간, 파일, 외부 이벤트를 기다리며 해당 조건을 충족해야만 이후의 작업을 진행할 수 있게 해주는 Airflow의 기능으로 Operator와 같이 하나의 task가 될 수 있으며 filesystem, hdfs, hive 등 다양한 형식을 제공한다.

- Taskflow: @task() 데코레이터를 통해 task를 패키징화 할 수 있음.

  https://data-engineer-tech.tistory.com/32



## Executor

- 각각의 Operator는 Executor에의해 실행됨.
- Executor를 지정함해서 다른 executor에서 실행할 수 있다.
  - sequentialexecutor(default)
    - task를 순차적으로 처리한다. sqlite3를 backend로 설정하여 실행
  - localexecutor
    - task를 병렬적으로 처리 가능. mysql, postgre 등을 backend로 지정해주어야함./ task마다 subprocess 생성
  - celeryexecutor
    - task를 여러 서버(node)에서 분산처리 가능하다. (cluster) / celery backend (redis, rabbitmq) 메시지 큐 브로커를 지정해주어야함.
  - Kubernetesexecutor
    - airflow cluster에서 task를 pod 형태로 실행시킨다.

## DAG 생성

### DAG 생성

- DAG 생성하는 흐름
  - (1) default_args 정의
    - 누가 만들었는지, start_date는 언제부턴지 등)
  - (2) DAG 객체 생성
    - dag id, schedule interval 정의
  - (3) DAG 안에 Operator를 활용해 Task 생성
  - (4) Task들을 연결함( `>>`, `<<` 활용)

- `xcom_push`, `xcom_pull`
  - Python Operator로 task를 수행할때 task간 data의 전달을 위해서는 Context의 ti (task_instance)에 xcom_push, xcom_pull 연산을 통해서 dataflow 데이터를 전달하게 된다.

## DAG 실행

- `airflow scheduler`  airflow scheduler를 실행해주어야 UI에서 실행한 Task들이 실행 될 수 있다.