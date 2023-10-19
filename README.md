# Solução para um Sistema de Pagamento com Spring Batch

<h2 align="center">
  <img width="250px" src="https://raw.githubusercontent.com/lucarauj/assets/main/Spring%20Batch.png">
</h2>

## Processamento de Arquivo CNAB com Spring Batch

### Dependências

- H2 Database
- Spring Batch
- Spring Web

### Anotações

- @Bean
- @Configuration
- @Controller
- @ControllerAdvice
- @RequestParam
- @RequestMapping
- @Service
- @Value

<br>

## Batch Config

<br>

```
@Bean
    Job job(Step step) {
        return new JobBuilder("job", jobRepository)
                .start(step)
                .incrementer(new RunIdIncrementer())
                .build();
    }
```
- cria e configura um trabalho (job).
- define uma etapa inicial, um incrementador para rastrear execuções do job e outras configurações necessárias.

<br>
<br>

```
@Bean
    Step step(ItemReader<TransacaoCNAB> reader, ItemProcessor<TransacaoCNAB, Transacao> processor, ItemWriter<Transacao> writer) {
        return new StepBuilder("step", jobRepository)
                .<TransacaoCNAB, Transacao>chunk(1000, transactionManager)
                .reader(reader)
                .processor(processor)
                .writer(writer)
                .build();
    }
```
- lê itens do tipo TransacaoCNAB, processa para obter Transacao e escreve os resultados. 
- processa os itens em lote, lendo 1000 itens por vez e usa um gerenciador de transações (transactionManager) para controle de transações.

<br>
<br>

```
@StepScope
    @Bean
    FlatFileItemReader<TransacaoCNAB> reader(@Value("#{jobParameters['cnabFile']}") Resource resource) {
        return new FlatFileItemReaderBuilder<TransacaoCNAB>()
                .name("reader")
                .resource(resource)
                .fixedLength()
                .columns(new Range(1, 1), new Range(2, 9), new Range(10, 19), new Range(20, 30),
                        new Range(31, 42), new Range(43, 48), new Range(49,62), new Range(63, 80))
                .names("tipo", "data", "valor", "cpf", "cartao","hora", "donoDaLoja", "nomeDaLoja")
                .targetType(TransacaoCNAB.class).build();
    }
```
- configura um leitor (reader) que lê dados de um arquivo plano (FlatFile), onde os dados são tratados como registros com campos fixos. 
- mapeia os dados para objetos do tipo TransacaoCNAB. 
- o leitor é criado com base em um recurso (resource) especificado no job parâmetros e é responsável por ler e transformar o conteúdo do arquivo em objetos TransacaoCNAB.

<br>
<br>

```
@Bean
    JdbcBatchItemWriter<Transacao> writer(DataSource dataSource) {
        return new JdbcBatchItemWriterBuilder<Transacao>()
                .dataSource(dataSource)
                .sql("""INSERT INTO transacao (tipo, data, valor, cpf, cartao, hora, dono_loja, nome_loja) 
			VALUES (:tipo, :data, :valor, :cpf, :cartao, :hora, :donoDaLoja, :nomeDaLoja)""")
                .beanMapped().build();
    }
```
- configura um escritor (writer) que escreve dados em lote. 
- o escritor é configurado para ler objetos do tipo Transacao e inseri-los no banco de dados especificado (dataSource). 
- o método .beanMapped() é usado para mapear automaticamente as propriedades do objeto Transacao para os parâmetros SQL correspondentes.

<br>
<br>

```
 @Bean
    JobLauncher jobLauncherAsync(JobRepository jobRepository) throws Exception {
        var jobLouncher = new TaskExecutorJobLauncher();
        jobLouncher.setJobRepository(jobRepository);
        jobLouncher.setTaskExecutor(new SimpleAsyncTaskExecutor());
        jobLouncher.afterPropertiesSet();
        return jobLouncher;
    }
```
- configura um lançador de trabalhos (job launcher) para execução assíncrona. 
- utiliza um TaskExecutorJobLauncher para iniciar trabalhos em um SimpleAsyncTaskExecutor, permitindo que os trabalhos sejam executados de forma assíncrona, em paralelo com outras tarefas, se necessário. 
- o jobRepository é configurado para lidar com o armazenamento e gerenciamento dos trabalhos. 

<br>
<br>

## Upload CNAB file:

```
public void uploadCnabFile(MultipartFile file) throws Exception {
        var fileName = StringUtils.cleanPath(file.getOriginalFilename());
        var targetLocation = fileStorageLocation.resolve(fileName);
        file.transferTo(targetLocation);

        var jobParameters = new JobParametersBuilder()
                .addJobParameter("cnab", file.getOriginalFilename(), String.class, true)
                .addJobParameter("cnabFile", "file:" + targetLocation.toString(), String.class)
                .toJobParameters();

        jobLauncher.run(job, jobParameters);
    }
```
- StringUtils.cleanPath(file.getOriginalFilename()): 
>*tem como função obter e limpar o nome original de um arquivo enviado por meio de um objeto MultipartFile.*

<br>

- fileStorageLocation.resolve(fileName):
>*cria um caminho completo (path) para o local de armazenamento do arquivo combinando o fileStorageLocation com o nome do arquivo limpo (fileName).*

<br>

- file.transferTo(targetLocation):
>*transfere o arquivo representado pelo objeto file para o local de destino especificado por targetLocation.*

<br>

- jobLauncher.run(job, jobParameters):
>*inicia a execução de um trabalho (job) em um sistema de processamento em lote (batch processing) usando o jobLauncher e os parâmetros definidos em jobParameters.*

<br>
<br>

# 👨🏼‍🎓 Aluno

Lucas Araujo

<a href="https://www.linkedin.com/in/lucarauj"><img alt="lucarauj | LinkdeIN" width="40px" src="https://user-images.githubusercontent.com/43545812/144035037-0f415fc7-9f96-4517-a370-ccc6e78a714b.png" /></a>
