package br.com.alura.screenmatch.repository;

import br.com.alura.screenmatch.model.Categoria;
import org.springframework.data.jpa.repository.JpaRepository;

import br.com.alura.screenmatch.model.Serie;
import org.springframework.data.jpa.repository.Query;

import java.awt.print.Pageable;
import java.util.List;
import java.util.Optional;


public interface SerieRepository extends JpaRepository<Serie, Long> {

    Optional<Serie> findByTituloContainingIgnoreCase(String nomeSerie);
    List<Serie> findAllByAtoresContainingIgnoreCase(String nomeAtor);
    List<Serie> findTop5ByOrderByAvaliacaoDesc();
    List<Serie> findByGenero(Categoria categoria);
    @Query("SELECT s FROM Serie s WHERE s.totalTemporadas <= :temporadas AND s.avaliacao >= :avaliacaoMinima")
    List<Serie> seriesPorTemporadaEAvaliacao(String temporadas, String avaliacaoMinima);
}
