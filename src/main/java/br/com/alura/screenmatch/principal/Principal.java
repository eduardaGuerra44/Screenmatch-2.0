package br.com.alura.screenmatch.principal;

import br.com.alura.screenmatch.model.*;
import br.com.alura.screenmatch.repository.SerieRepository;
import br.com.alura.screenmatch.service.ConsumoApi;
import br.com.alura.screenmatch.service.ConverteDados;


import java.util.*;
import java.util.stream.Collectors;



public class Principal {

    private Scanner leitura = new Scanner(System.in);
    private ConsumoApi consumo = new ConsumoApi();
    private ConverteDados conversor = new ConverteDados();
    private final String ENDERECO = "https://www.omdbapi.com/?t=";
    private final String API_KEY = System.getenv("API_KEY");
    private List<DadosSerie> dadosSeries = new ArrayList<>();
    private List<Serie> series = new ArrayList<>();

    private Optional<Serie> serieBusca;

    private final SerieRepository repositorio;

    public Principal(SerieRepository repositorio) {
        this.repositorio = repositorio;
    }

    public void exibeMenu() {
        var opcao = -1;
        while(opcao !=0) {
            var menu = """
                    1 - Buscar séries
                    2 - Buscar episódios
                    3 - Listar series buscadas   
                    4 - Buscar série por titulo  
                    5 - Buscar série por ator  
                    6 - Buscar top 5 séries  
                    7 - Buscar série por categoria    
                    8 - Filtrar series
                    9 - Busca Episodio por trecho
                    10 - Top cinco Episodios por Série
                    11 - busca episódio por data
                    0 - Sair                                 
                    """;

            System.out.println(menu);
            opcao = leitura.nextInt();
            leitura.nextLine();

            switch (opcao) {
                case 1:
                    buscarSerieWeb();
                    break;
                case 2:
                    buscarEpisodioPorSerie();
                    break;
                case 3:
                    listarSeriesBuscadas();
                    break;
                case 4:
                    buscarSeriePorTitulo();
                    break;
                case 5:
                    buscaSeriePorAtor();
                    break;
                case 6:
                    buscarTopCincoSeries();
                    break;
                case 7:
                    buscarPorCategoria();
                    break;
                case 8:
                    fitrarSeriesPorTemporadasEAvaliacao();
                    break;
                case 9:
                    buscaEpisodioPorTrecho();
                    break;
                case 10:
                    topEpisodioPorSerie();
                    break;
                case 11:
                    buscarEpisodioDepoisDeUmaData();
                    break;
                case 0:
                    System.out.println("Saindo...");
                    break;
                default:
                    System.out.println("Opção inválida");
            }
        }
    }

    private void buscarSerieWeb() {
        DadosSerie dados = getDadosSerie();
        Serie serie = new Serie(dados);
        repositorio.save(serie);
        System.out.println(dados);
    }

    private DadosSerie getDadosSerie() {
        System.out.println("Digite o nome da série para busca");
        var nomeSerie = leitura.nextLine();
        var json = consumo.obterDados(ENDERECO + nomeSerie.replace(" ", "+") + API_KEY);
        DadosSerie dados = conversor.obterDados(json, DadosSerie.class);
        return dados;
    }

    private void buscarEpisodioPorSerie(){
        listarSeriesBuscadas();
        System.out.println("Escolha uma série pelo nome");
        var nomeSerie = leitura.nextLine();

        Optional<Serie> serie= repositorio.findByTituloContainingIgnoreCase(nomeSerie);


        if (serie.isPresent()){
            var serieEncontrada = serie.get();
            List<DadosTemporada> temporadas = new ArrayList<>();

        for (int i = 1; i <= serieEncontrada.getTotalTemporadas(); i++) {
            var json = consumo.obterDados(ENDERECO + serieEncontrada.getTitulo().replace(" ", "+") + "&season=" + i + API_KEY);
            DadosTemporada dadosTemporada = conversor.obterDados(json, DadosTemporada.class);
            temporadas.add(dadosTemporada);
        }
        temporadas.forEach(System.out::println);

       List<Episodio> episodios = temporadas.stream()
                .flatMap(d -> d.episodios().stream()
                .map(e -> new Episodio(d.numero(), e)))
                .collect(Collectors.toList());
       serieEncontrada.setEpisodios(episodios);
       repositorio.save(serieEncontrada);
    } else {
            System.out.println("Série não encontrada");
        }
    }

    private void listarSeriesBuscadas() {
        series = repositorio.findAll();
        series.stream()
                .sorted(Comparator.comparing(Serie::getGenero))
                .forEach(System.out::println);
    }

    private void buscarSeriePorTitulo() {
        System.out.println("Busque por um titulo: ");
        var nomeSerie = leitura.nextLine();
        serieBusca = repositorio.findByTituloContainingIgnoreCase(nomeSerie);

        if(serieBusca.isPresent()){
            System.out.println("Dados da Série: " + serieBusca.get());
        } else {
            System.out.println("Série não encontrada!");
        }

    }

    private void buscaSeriePorAtor() {
        System.out.println("Busque por um ator: ");
        var nomeAutor = leitura.nextLine();
        List<Serie> seriesEncontradas = repositorio.findAllByAtoresContainingIgnoreCase(nomeAutor);

        System.out.println("Séries em que o autor " + nomeAutor + " trabalhou: ");
        seriesEncontradas.forEach(
                s -> System.out.println(s.getTitulo() + ", avaliação: " + s.getAvaliacao())
        );
    }

    private void buscarTopCincoSeries(){
        System.out.println("Veja as top 5 séries");
        List<Serie> topCinco = repositorio.findTop5ByOrderByAvaliacaoDesc();

        topCinco.forEach(
                serie -> System.out.println(serie.getTitulo() + " avaliação: " + serie.getAvaliacao())
        );
    }

    private void buscarPorCategoria(){
        System.out.println("Busque sua série por categoria / gênero");
        var nomeCategoria = leitura.nextLine();
        var categoria = Categoria.fromPortugues(nomeCategoria);
        List<Serie> seriesPorCategoria = repositorio.findByGenero(categoria);
        System.out.println("Séries da categoria: " + nomeCategoria);
        seriesPorCategoria.forEach(serie -> System.out.println(serie.getTitulo()));
    }

    private void fitrarSeriesPorTemporadasEAvaliacao() {
        System.out.println("Digite o máximo de temporadas:");
        var temporadas = leitura.nextLine();
        System.out.println("Digite a avalição mínima para a série:");
        var avaliacaoMinima = leitura.nextLine();

        List<Serie> filtraPorTemporadaEAvaliacao = repositorio.seriesPorTemporadaEAvaliacao(temporadas, avaliacaoMinima);
        System.out.println("***** SERIES FILTRADAS *******");
        filtraPorTemporadaEAvaliacao.forEach( serie -> System.out.println(serie.getTitulo() + " - avaliação: " + serie.getAvaliacao()));
    }

    private void buscaEpisodioPorTrecho() {
        System.out.println("Qual o nome do episódio para busca?");
        var trechoEpisodio = leitura.nextLine();
        List<Episodio> episodiosEncontrados = repositorio.episodioPorTrecho(trechoEpisodio);
        episodiosEncontrados.forEach( e -> System.out.printf( "Série: %s Temporada %s - Episodio %s - %s\n",
                e.getSerie().getTitulo(), e.getTemporada(), e.getNumeroEpisodio(), e.getTitulo()));

    }

   private void topEpisodioPorSerie() {
        buscarSeriePorTitulo();
        if(serieBusca.isPresent()){
           Serie serie =  serieBusca.get();
           List<Episodio> topEpisodios = repositorio.topEpisodioPorSerie(serie);
            topEpisodios.forEach( e -> System.out.printf( "Série: %s Temporada %s - Episodio %s - %s Avaliação: %s\n",
                    e.getSerie().getTitulo(), e.getTemporada(), e.getNumeroEpisodio(), e.getTitulo(), e.getAvaliacao()));
        }
   }

    private void buscarEpisodioDepoisDeUmaData() {
        buscarSeriePorTitulo();
        if (serieBusca.isPresent()){
            Serie serie = serieBusca.get();
            System.out.println("Digite o ano limite de lançamento ");
            var anoLancamento = leitura.nextInt();
            leitura.nextLine();

        List<Episodio> episodios = repositorio.episodioPorSerieEAno(serie, anoLancamento);
            episodios.forEach(System.out::println);
        }
    }
}