package it.polito.tdp.imdb.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jgrapht.Graph;
import org.jgrapht.Graphs;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleWeightedGraph;

import it.polito.tdp.imdb.db.ImdbDAO;

public class Model {

	private ImdbDAO dao;
	private Graph<Director, DefaultWeightedEdge> grafo;
	private Map<Integer, Director> idMap;
	private List<Director> vertici;
	private List<Adiacenza> adiacenze;
	//strutture per la ricorsione
	private List<Director> best;
	private int sommaPesiMax;
	
	public Model() {
		this.dao = new ImdbDAO();
		this.idMap = new HashMap<>();
		this.idMap = this.dao.listAllDirectors();
	}
	
	public void creaGrafo(Integer anno) {
		this.grafo = new SimpleWeightedGraph<>(DefaultWeightedEdge.class);
		//aggiungo i vertici
		this.vertici = new ArrayList<>(this.dao.getDirectorYear(anno, idMap));
		Graphs.addAllVertices(this.grafo, this.vertici);
		//Aggiungi gli archi
		this.adiacenze = new ArrayList<>(this.dao.getArchi(anno, idMap));
		for(Adiacenza aa : this.adiacenze) {
			Graphs.addEdgeWithVertices(this.grafo, aa.getD1(), aa.getD2(), aa.getPeso());
		}
	}
	public boolean isGraphCreated() {
		if(this.grafo == null) {
			return false;
		}
		return true;
	}
	public int nVertices() {
		return this.grafo.vertexSet().size();
	}
	public int nArchi() {
		return this.grafo.edgeSet().size();
	}
	public List<Director> getRegisti(){
		return this.vertici;
	}
	
	public List<Adiacenza> getAdiacenti(Director regista){
		List<Adiacenza> adiacenti = new ArrayList<>();
		List<Director> vicini = Graphs.neighborListOf(this.grafo, regista);
		for(Director vicino : vicini) {
			DefaultWeightedEdge e = this.grafo.getEdge(regista, vicino);
			int peso = (int)this.grafo.getEdgeWeight(e);
			adiacenti.add(new Adiacenza(regista, vicino, peso));
		}
		Collections.sort(adiacenti);
		return adiacenti;
	}
	
	public List<Director> cercaCammino(Director r, int c) {
		List<Director> parziale = new ArrayList<>();
		parziale.add(r);
		this.best = new ArrayList<>();
		this.sommaPesiMax = 0;
		ricerca(parziale, c);
		return this.best;
	}

	private void ricerca(List<Director> parziale, int c) {
		if(parziale.size() > this.best.size()) {
			this.best = new ArrayList<>(parziale);
			this.sommaPesiMax = this.sommaPesi(parziale);
		}
		Director ultimo = parziale.get(parziale.size()-1);
		List<Director> adiacenti = Graphs.neighborListOf(this.grafo, ultimo);
		for(Director d : adiacenti) {
			if(!parziale.contains(d) && this.aggiuntaValida(parziale, d, ultimo, c)) {
				parziale.add(d);
				ricerca(parziale, c);
				parziale.remove(d);
			}
		}
		
	}
	
	private boolean aggiuntaValida(List<Director> parziale, Director prova, Director ultimo, int c) {
		int somma = this.sommaPesi(parziale);
		DefaultWeightedEdge e = this.grafo.getEdge(ultimo, prova);
		int pesoNew = (int)this.grafo.getEdgeWeight(e);
		if( (somma+pesoNew) <= c) {
			return true;
		}else {
			return false;
		}
	}
	
	private int sommaPesi(List<Director> parziale) {
		int somma = 0;
		for(int i = 1; i < parziale.size() && parziale.size() >=2; i++) {
			Director s = parziale.get(i-1);
			Director d = parziale.get(i);
			DefaultWeightedEdge e = this.grafo.getEdge(s, d);
			int peso = (int)this.grafo.getEdgeWeight(e);
			somma += peso;
		}
		return somma;
	}
	
	public int getSommaMax() {
		return this.sommaPesiMax;
	}
	
	
}
