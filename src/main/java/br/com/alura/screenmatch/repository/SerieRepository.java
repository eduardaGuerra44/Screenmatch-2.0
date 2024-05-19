package br.com.alura.screenmatch.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.alura.screenmatch.model.*;


import java.util.Set;

public interface SerieRepository extends JpaRepository<Serie, Long> {

}
