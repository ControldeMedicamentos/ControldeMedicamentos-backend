package com.medicamentos.repository;

import com.medicamentos.domain.model.Usuario;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    Optional<Usuario> findByUsername(String username);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    Optional<Usuario> findByEmail(String email);

    Optional<Usuario> findByResetToken(String resetToken);

    @Query("""
            SELECT u FROM Usuario u
            WHERE (:estado = 'todos'
                OR (:estado = 'activos' AND u.activo = true)
                OR (:estado = 'inactivos' AND u.activo = false))
              AND (:search = ''
                OR LOWER(u.nombre) LIKE LOWER(CONCAT('%', :search, '%'))
                OR LOWER(u.username) LIKE LOWER(CONCAT('%', :search, '%'))
                OR LOWER(u.email) LIKE LOWER(CONCAT('%', :search, '%'))
                OR LOWER(CAST(u.rol AS string)) LIKE LOWER(CONCAT('%', :search, '%')))
            """)
    Page<Usuario> findPage(
            @Param("search") String search,
            @Param("estado") String estado,
            Pageable pageable
    );
}
