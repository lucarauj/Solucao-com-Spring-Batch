# Solução para um Sistema de Pagamento com Spring Batch

<h2 align="center">
  <img width="250px" src="https://raw.githubusercontent.com/lucarauj/assets/main/Spring%20Batch.png">
</h2>

## Processamento de Arquivo CNAB com Spring Batch

### Dependências

- H2 Database
- Spring Batch
- Spring Web
- Spring Data JDBC

### Anotações

- @Bean
- @Column
- @Configuration
- @Controller
- @ControllerAdvice
- @CrossOrigin
- @ExtendWith
- @GetMapping
- @Id
- @InjectMocks
- @Mock
- @RequestParam
- @RequestMapping
- @Service
- @Test
- @Value

<br>

## Batch Config

<hr>
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

<hr>
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

<hr>
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

<hr>
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

<hr>
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

<hr>
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

<hr>
<br>

## Agrupando pesquisa por nome da loja e valor total:

```
public List<TransacaoReport> listTransacoesTotaisPorNomeDaLoja() {
        var transacoes = repository.findAllByOrderByNomeDaLojaAscIdDesc();

        var reportMap = new LinkedHashMap<String, TransacaoReport>();

        transacoes.forEach(transacao -> {
            String nomeDaLoja = transacao.nomeDaLoja();
            BigDecimal valor = transacao.valor();

            reportMap.compute(nomeDaLoja, (key, existingReport) -> {
                var report = (existingReport != null) ? existingReport :
                        new TransacaoReport(key, BigDecimal.ZERO, new ArrayList<>());
                return report.addTotal(valor).addTransacao(transacao);
            });
        });
        return new ArrayList<>(reportMap.values());
    }
```

- var transacoes = repository.findAllByOrderByNomeDaLojaAscIdDesc():
>*busca no banco todas as transações.*

<br>

- var reportMap = new LinkedHashMap<String, TransacaoReport>():
>*cria um mapa para utilizar o nome da loja como chave.*

<br>

- reportMap.compute(nomeDaLoja, (key, existingReport) -> { var report = (existingReport != null) ? existingReport : new TransacaoReport(key, BigDecimal.ZERO, new ArrayList<>()):
>*cria um novo objeto TransacaoReport se ainda não houver um existente no mapa com a mesma chave. Esse objeto será usado para acumular os totais.*

<br>

## Aplicando testes com JUnit

- AAA 

>*Arrange: fase onde o ambiente de teste é preparado, configurando os objetos e definindo o estado inicial necessário para o teste.*

>*Act: fase onde é executada a ação ou operação que deseja testar. Este passo representa a interação com o sistema ou componente que está sendo testado.*

>*Assert: fase onde é verificado se o resultado da ação realizada no passo anterior está de acordo com o esperado. Isso envolve a utilização de asserções para confirmar se o comportamento do sistema é conforme o planejado.*

<br>

## Front-end com Vite + React

- Instalação do Vite:
>*npm install -g create-vite*

- Criando o projeto:
>*npx create-vite "nome-do-projeto" --template react*

- Baixando as dependências:
>*npm install*

- Rodando o projeto:
>*npm run dev*

- Instalando a biblioteca Axios:
>*npm instal axios*

<br>

# 👨🏼‍🎓 Aluno

Lucas Araujo

<a href="https://www.linkedin.com/in/lucarauj"><img alt="lucarauj | LinkdeIN" width="40px" src="https://user-images.githubusercontent.com/43545812/144035037-0f415fc7-9f96-4517-a370-ccc6e78a714b.png" /></a>
