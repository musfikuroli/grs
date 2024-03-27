package com.grs.core.domain.grs;

import com.grs.core.domain.BaseEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.NotFound;
import org.hibernate.annotations.NotFoundAction;

import javax.persistence.*;
import java.util.List;

/**
 * Created by Acer on 9/9/2017.
 */
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "grs_roles")
public class GrsRole extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(length = 50, name = "role")
    private String role;

    @NotFound(action = NotFoundAction.IGNORE)
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "permissions_to_roles",
            joinColumns = @JoinColumn(name = "role_id", referencedColumnName = "ID"),
            inverseJoinColumns = @JoinColumn(name = "permission_id", referencedColumnName = "ID"))
    private List<Permission> permissions;
}
