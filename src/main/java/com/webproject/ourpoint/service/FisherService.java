package com.webproject.ourpoint.service;

import com.webproject.ourpoint.errors.NotAcceptableException;
import com.webproject.ourpoint.errors.UnauthorizedException;
import com.webproject.ourpoint.model.common.Id;
import com.webproject.ourpoint.model.liked.Liked;
import com.webproject.ourpoint.model.marker.Marker;
import com.webproject.ourpoint.model.user.Fisher;
import com.webproject.ourpoint.model.user.Role;
import com.webproject.ourpoint.repository.FisherRepository;
import com.webproject.ourpoint.repository.LikedRepository;
import com.webproject.ourpoint.repository.MarkerRepository;
import com.webproject.ourpoint.utils.PasswordValidation;
import com.webproject.ourpoint.errors.NotFoundException;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;

import static com.google.common.base.Preconditions.checkArgument;
import static com.webproject.ourpoint.utils.EmailFormatValidation.checkAddress;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;


@Service
public class FisherService {

  private final PasswordEncoder passwordEncoder;

  private final FisherRepository fisherRepository;

  private final MarkerRepository markerRepository;

  private final LikedRepository likedRepository;

  public FisherService(PasswordEncoder passwordEncoder, FisherRepository fisherRepository, MarkerRepository markerRepository, LikedRepository likedRepository) {
    this.passwordEncoder = passwordEncoder;
    this.fisherRepository = fisherRepository;
    this.markerRepository = markerRepository;
    this.likedRepository = likedRepository;
  }


  //----------------------------Transactional method--------------------------------

  @Transactional
  private Fisher save(Fisher fisher) {
    return fisherRepository.save(fisher);
  }

  @Transactional
  public Fisher join(String email, String password, String profImageName, String name) {
    //email 중복?
    checkArgument(findByEmail(email).isEmpty(),"This email already exist.");
    //email 형식 맞음?
    try {
      checkArgument(checkAddress(email), "Invalid email address: " ,email);
    } catch (Exception e) {
      throw new NotAcceptableException("Invalid email address: " + email);
    }
    //닉네임 중복?
    checkArgument(findByName(name).isEmpty(), "This name already exist.");
    //비밀번호 형식에 맞음?
    try {
      checkArgument(PasswordValidation.isValidPassword(password), "password validation failed.");
    } catch (Exception e) {
      throw new NotAcceptableException("invalid Password");
    }
      //닉네임 2 ~ 10?
    try {
      checkArgument(
              name.length() >= 2 && name.length() <= 10,
              "name length must be between 2 and 10 characters."
      );
    } catch (Exception e) {
      throw new NotAcceptableException("Invalid name length.");
    }

    Fisher fisher;

    if (email.equals("icetime963@gmail.com"))
      fisher = new Fisher(email,passwordEncoder.encode(password), profImageName, name,Role.ADMIN.name());
    else
      fisher = new Fisher(email,passwordEncoder.encode(password), profImageName, name, Role.FISHER.name());
    return save(fisher);
  }


  @Transactional
  public Fisher login(String email, String password) {
    checkArgument(isNotEmpty(password), "password must be provided.");
    checkArgument(isNotEmpty(email),"email must be provided.");

      Fisher fisher = findByEmail(email).orElseThrow(() -> new NotFoundException("찾을 수 없습니다."));
    try {
      checkArgument(fisher.login(passwordEncoder, password),"비밀번호가 일치하지 않습니다.");
    } catch (Exception e) {
      throw new NotAcceptableException("Not Matched");
    }
      save(fisher);
      return fisher;
    }

    @Transactional
    @Modifying(clearAutomatically = true)
    public Fisher changeName(Id<Fisher,Long> id, String changeName) {
      Fisher fisher = findById(id).orElseThrow(() -> new NotFoundException("찾을 수 없습니다."));
      try {
        checkArgument(findByName(changeName).isEmpty(),"This email already exist.");
        checkArgument(changeName.length() >= 2 && changeName.length() <= 10, "Invalid name length.");
      } catch (Exception e) {
        throw new NotAcceptableException("This email already exist.");
      }
      fisher.setFisherName(changeName);
      save(fisher);
      return fisher;
    }

    @Transactional
    @Modifying(clearAutomatically = true)
    public Fisher changePassword(Id<Fisher,Long> id, String password, String changePassword) {
      Fisher fisher = findById(id).orElseThrow(() -> new NotFoundException("찾을 수 없습니다."));
      try{
        checkArgument(fisher.isPasswordMatch(passwordEncoder, password),"not matched.");
      } catch (Exception e) {
        throw new UnauthorizedException("Not matched.");
      }
      try {
        checkArgument(PasswordValidation.isValidPassword(changePassword), "password validation failed.");
      } catch (Exception e) {
        throw new NotAcceptableException("Password validation failed.");
      }
      fisher.setPassword(passwordEncoder.encode(changePassword));
      save(fisher);
      return fisher;
    }

    @Transactional
    public void delete(Id<Fisher,Long> id, String email, String password) {
      Fisher fisher = findById(id).orElseThrow(() -> new NotFoundException("찾을 수 없습니다."));
      checkArgument(fisher.getEmail().equals(email), "이메일이 일치하지 않습니다.");
      checkArgument(fisher.isPasswordMatch(passwordEncoder, password), "비밀번호가 일치하지 않습니다.");
      fisherRepository.delete(fisher);
      cascadeLikes(id);
      cascadeMarkers(id);
    }

    // 이 메소드는 관리자 전용입니다.
    @Transactional
    @Modifying(clearAutomatically = true)
    public Fisher changeRole(Id<Fisher,Long> id, String fisherName, String changeRole) {
      checkArgument(findById(id).orElseThrow(() -> new NotFoundException("찾을 수 없습니다."))
              .getRole().equals(Role.ADMIN.name()),"관리자가 아닙니다.",HttpStatus.UNAUTHORIZED);

      Fisher fisher = findByName(fisherName).orElseThrow(() -> new NotFoundException("찾을 수 없습니다."));

      Role role;

      if (changeRole.toUpperCase(Locale.ROOT).equals("GOODFISHER")) {
        role = Role.GOODFISHER;
      }
      else if (changeRole.toUpperCase(Locale.ROOT).equals("GREATFISHER")) {
        role = Role.GREATFISHER;
      }
      else if (changeRole.toUpperCase(Locale.ROOT).equals("FISHER")) {
        role = Role.FISHER;
      }
      else {
        throw new NotFoundException("찾을 수 없습니다.");
      }

      fisher.setRole(role.name());
      save(fisher);
      return fisher;
    }

    //-------------------------read only methods------------------------------------

    @Transactional(readOnly = true)
    public Optional<Fisher> findById(Id<Fisher, Long> fisherId) {
      checkArgument(fisherId != null, "markerId must be provided.",HttpStatus.BAD_REQUEST);

      return fisherRepository.findById(fisherId.value());
    }

    @Transactional(readOnly = true)
    public Optional<Fisher> findByEmail(String email) {
      try {
        checkArgument(checkAddress(email), "Invalid email address: " + email, HttpStatus.BAD_REQUEST);
      } catch (Exception e) {
        throw new NotAcceptableException("email validation fail.");
      }
      checkArgument(email != null, "email must be provided.",HttpStatus.BAD_REQUEST);

      return Optional.ofNullable(fisherRepository.findByEmail(email));
    }

    @Transactional(readOnly = true)
    public Optional<Fisher> findByName(String name) {
      checkArgument(name != null, "name must be provided.",HttpStatus.BAD_REQUEST);

      return Optional.ofNullable(fisherRepository.findByName(name));
    }

    private void cascadeMarkers(Id<Fisher,Long> id) {
      List<Marker> markers = markerRepository.findAll();
      markers.forEach((element) -> {
        if (Objects.equals(element.getFisherId(), id.value())) {
          markerRepository.delete(element);
        }
      });
    }

    private void cascadeLikes(Id<Fisher,Long> id) {
      List<Liked> likeds = likedRepository.findAll();
      likeds.forEach((element) -> {
        if (Objects.equals(element.getFisherId(),id.value()) || Objects.equals(element.getMfId(),id.value())) {
          likedRepository.delete(element);
        }
      });
    }
  }
