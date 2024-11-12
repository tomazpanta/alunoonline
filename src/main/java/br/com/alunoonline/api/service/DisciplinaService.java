package br.com.alunoonline.api.service;

import br.com.alunoonline.api.model.Aluno;
import br.com.alunoonline.api.model.Disciplina;
import br.com.alunoonline.api.repository.DisciplinaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

@Service
public class DisciplinaService {

    @Autowired
    DisciplinaRepository disciplinaRepository;

    public void criarDisciplina(Disciplina disciplina) {
        disciplinaRepository.save(disciplina);
    }

    public List<Disciplina> listarTodasDisciplinas() {
        return disciplinaRepository.findAll();
    }

    public Optional<Disciplina> buscarDisciplinaPorId(Long id) {
        return disciplinaRepository.findById(id);
    }

    public void deletarDisciplinaPorId(Long id) {
        disciplinaRepository.deleteById(id);
    }

    public void atualizarDisciplinaPorId(Long id, Disciplina disciplina) {
        // PRIMEIRO PASSO: VER SE O DISCIPLINA EXISTE NO BD
        Optional<Disciplina> disciplinaDoBancoDeDados = buscarDisciplinaPorId(id);

        // E SE NÃO EXISTIR???
        if (disciplinaDoBancoDeDados.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Disciplina não encontrada no banco de dados");
        }

        // SE CHEGAR AQUI, SIGNIFICA QUE EXISTE DISCIPLINA! ENTÃO
        // VOU ARMAZENA-LO EM UMA VARIÁVEL
        Disciplina disciplinaEditada = disciplinaDoBancoDeDados.get();

        // COM ESSE DISCIPLINA EITADA DE CIMA, FAÇO
        // OS SETS NECESSÁRIOS PARA ATUALIZAR OS ATRIBUTOS DELA.
        disciplinaEditada.setNome(disciplina.getNome());

        // COM O ALUNO TOTALMENTE EDITADO ACIMA
        // EU DEVOLVO ELE EDITADO/ATUALIZADO PARA O BANCO DE DADOS
        disciplinaRepository.save(disciplinaEditada);
    }

    public List<Disciplina> listarDisciplinasDoProf(Long professorId){
        return disciplinaRepository.findByProfessorId(professorId);
    }

}
