package com.three.ott_suggestion.image.repository;

import com.three.ott_suggestion.image.PostImage;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostImageRepository extends JpaRepository<PostImage, Long> {

}
