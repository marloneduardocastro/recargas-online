package co.com.recargas.service;

import co.com.recargas.dto.OperadorDto;
import co.com.recargas.dto.VendedorDto;
import co.com.recargas.repository.OperadorRepository;
import co.com.recargas.repository.VendedorRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class CatalogoService {

    private final OperadorRepository operadorRepository;
    private final VendedorRepository vendedorRepository;

    public CatalogoService(OperadorRepository operadorRepository, VendedorRepository vendedorRepository) {
        this.operadorRepository = operadorRepository;
        this.vendedorRepository = vendedorRepository;
    }

    public List<OperadorDto> operadoresActivos() {
        return operadorRepository.findByActivoTrueOrderByNombreAsc().stream()
                .map(OperadorDto::from)
                .toList();
    }

    public List<VendedorDto> vendedoresActivos() {
        return vendedorRepository.findByActivoTrueOrderByNombreAsc().stream()
                .map(VendedorDto::from)
                .toList();
    }
}
