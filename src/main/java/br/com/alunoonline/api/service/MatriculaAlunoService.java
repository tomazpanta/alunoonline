package br.com.alunoonline.api.service;

import br.com.alunoonline.api.dtos.AtualizarNotasRequest;
import br.com.alunoonline.api.dtos.DisciplinasAlunoResponse;
import br.com.alunoonline.api.dtos.HistoricoAlunoResponse;
import br.com.alunoonline.api.enums.MatriculaAlunoStatusEnum;
import br.com.alunoonline.api.model.MatriculaAluno;
import br.com.alunoonline.api.repository.MatriculaAlunoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;

@Service
public class MatriculaAlunoService {

    public static final double MEDIA_PARA_APROVACAO = 7.0;

    @Autowired
    MatriculaAlunoRepository matriculaAlunoRepository;

    /*
    É aqui que o aluno vai se matricular
     */
    public void criarMatricula(MatriculaAluno matriculaAluno) {
        matriculaAluno.setStatus(MatriculaAlunoStatusEnum.MATRICULADO);
        matriculaAlunoRepository.save(matriculaAluno);
    }

    /* É aqui que o aluno vai trancar sua matricula em alguma disciplina */


    public void trancarMatricula(Long matriculaAlunoId) {
        MatriculaAluno matriculaAluno = matriculaAlunoRepository.findById(matriculaAlunoId).orElseThrow(()
                -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Matricula Aluno não encontrada!"));

        if (!MatriculaAlunoStatusEnum.MATRICULADO.equals(matriculaAluno.getStatus())) {
            // Lançar o erro se o status não for matriculado
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Só é possível trancar uma matricula com o status MATRICULADO!");

        }

        matriculaAluno.setStatus(MatriculaAlunoStatusEnum.TRANCADO);

        matriculaAlunoRepository.save(matriculaAluno);
    }

    private Double calculaMedia(Double nota1, Double nota2) {
        if (nota1 != null && nota2 != null) {
            return (nota1 + nota2) / 2.0;
        }
        return null;
    }

    public void atualizaNotas(Long matriculaAlunoId, AtualizarNotasRequest atualizarNotasRequest) {
        MatriculaAluno matriculaAluno = matriculaAlunoRepository.findById(matriculaAlunoId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Matricula Aluno não encontrada!"));

        if (atualizarNotasRequest.getNota1() != null) {
            matriculaAluno.setNota1(atualizarNotasRequest.getNota1());
        }
        if (atualizarNotasRequest.getNota2() != null) {
            matriculaAluno.setNota2(atualizarNotasRequest.getNota2());
        }

        Double media = calculaMedia(matriculaAluno.getNota1(), matriculaAluno.getNota2());
        if (media != null) {
            matriculaAluno.setStatus(media >= MEDIA_PARA_APROVACAO ? MatriculaAlunoStatusEnum.APROVADO : MatriculaAlunoStatusEnum.REPROVADO);
        }

        matriculaAlunoRepository.save(matriculaAluno);
    }

    public HistoricoAlunoResponse emitirHistorico(Long alunoId) {
        List<MatriculaAluno> matriculasDoAluno = matriculaAlunoRepository.findByAlunoId(alunoId);

        if (matriculasDoAluno.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Esse aluno não possui matriculas");
        }

        HistoricoAlunoResponse historicoAluno = new HistoricoAlunoResponse();
        historicoAluno.setNomeAluno(matriculasDoAluno.get(0).getAluno().getNome());
        historicoAluno.setCpfAluno(matriculasDoAluno.get(0).getAluno().getCpf());
        historicoAluno.setEmailAluno(matriculasDoAluno.get(0).getAluno().getEmail());

        List<DisciplinasAlunoResponse> disciplinasList = new ArrayList<>();

        for (MatriculaAluno matriculaAluno : matriculasDoAluno) {
            DisciplinasAlunoResponse disciplinasAlunoResponse = new DisciplinasAlunoResponse();
            disciplinasAlunoResponse.setNomeDisciplina(matriculaAluno.getDisciplina().getNome());
            disciplinasAlunoResponse.setNomeProfessor(matriculaAluno.getDisciplina().getProfessor().getNome());
            disciplinasAlunoResponse.setNota1(matriculaAluno.getNota1());
            disciplinasAlunoResponse.setNota2(matriculaAluno.getNota2());

            Double media = calculaMedia(matriculaAluno.getNota1(), matriculaAluno.getNota2());
            disciplinasAlunoResponse.setMedia(media);
            disciplinasAlunoResponse.setStatus(matriculaAluno.getStatus());

            disciplinasList.add(disciplinasAlunoResponse);
        }

        historicoAluno.setDisciplinasAlunoResponses(disciplinasList);

        return historicoAluno;
    }


}
