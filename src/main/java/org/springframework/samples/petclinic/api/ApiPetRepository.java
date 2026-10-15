package org.springframework.samples.petclinic.api;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.samples.petclinic.owner.Owner;
import org.springframework.samples.petclinic.owner.Pet;

interface ApiPetRepository extends JpaRepository<Pet, Integer> {

	@Query("select p from Owner o join o.pets p where o.id = :ownerId order by p.name")
	List<Pet> findByOwnerId(@Param("ownerId") Integer ownerId);

	@Query("select o from Owner o join o.pets p where p.id = :petId")
	Optional<Owner> findOwnerByPetId(@Param("petId") Integer petId);

}
