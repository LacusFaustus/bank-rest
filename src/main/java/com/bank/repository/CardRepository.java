package com.bank.repository;

import com.bank.entity.Card;
import com.bank.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface CardRepository extends JpaRepository<Card, Long> {
    Page<Card> findByUser(User user, Pageable pageable);

    @Query("SELECT c FROM Card c WHERE c.user = :user AND " +
            "(LOWER(c.cardHolder) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "CAST(c.id AS string) LIKE CONCAT('%', :search, '%'))")
    Page<Card> findByUserAndSearch(@Param("user") User user,
                                   @Param("search") String search,
                                   Pageable pageable);

    List<Card> findByUserAndStatus(User user, Card.CardStatus status);

    List<Card> findByStatusAndExpiryDateBefore(Card.CardStatus status, LocalDate expiryDate);

    @Query("SELECT c FROM Card c WHERE c.user = :user AND c.status = 'ACTIVE' AND c.expiryDate > CURRENT_DATE")
    List<Card> findActiveUserCards(User user);

    Page<Card> findAll(Pageable pageable);

    Optional<Card> findByIdAndUser(Long id, User user);

    boolean existsByCardNumber(String cardNumber);

    @Query("SELECT COUNT(c) > 0 FROM Card c WHERE c.id = :cardId AND c.user.id = :userId")
    boolean existsByIdAndUserId(@Param("cardId") Long cardId, @Param("userId") Long userId);

    List<Card> findByBlockRequestedTrue();

    // Упрощенная версия фильтрации - убираем сложный boolean expression
    @Query("SELECT c FROM Card c WHERE " +
            "(:status IS NULL OR c.status = :status) AND " +
            "(:userId IS NULL OR c.user.id = :userId)")
    Page<Card> findWithFilters(@Param("status") Card.CardStatus status,
                               @Param("userId") Long userId,
                               Pageable pageable);

    // Отдельный метод для поиска просроченных карт
    @Query("SELECT c FROM Card c WHERE c.expiryDate < CURRENT_DATE")
    Page<Card> findExpiredCards(Pageable pageable);

    // Отдельный метод для поиска активных не просроченных карт
    @Query("SELECT c FROM Card c WHERE c.expiryDate >= CURRENT_DATE AND c.status = 'ACTIVE'")
    Page<Card> findActiveNonExpiredCards(Pageable pageable);

    @Query("SELECT COUNT(c) FROM Card c WHERE c.user.id = :userId AND c.status = 'ACTIVE'")
    long countActiveCardsByUserId(@Param("userId") Long userId);

    // Упрощенный метод для поиска по имени держателя карты
    default Page<Card> findByUserAndCardHolderContainingIgnoreCase(User user, String search, Pageable pageable) {
        return findByUserAndSearch(user, search, pageable);
    }
}
