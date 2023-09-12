package com.webproject.flarepoint.service;

import com.webproject.flarepoint.errors.NotAcceptableException;
import com.webproject.flarepoint.errors.UnauthorizedException;
import com.webproject.flarepoint.model.common.Id;
import com.webproject.flarepoint.model.liked.Liked;
import com.webproject.flarepoint.model.marker.Marker;
import com.webproject.flarepoint.model.user.User;
import com.webproject.flarepoint.model.user.Role;
import com.webproject.flarepoint.repository.UserRepository;
import com.webproject.flarepoint.repository.LikedRepository;
import com.webproject.flarepoint.repository.MarkerRepository;
import com.webproject.flarepoint.utils.PasswordValidation;
import com.webproject.flarepoint.errors.NotFoundException;
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
import static com.webproject.flarepoint.utils.EmailFormatValidation.checkAddress;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;


@Service
public class UserService {

  private final PasswordEncoder passwordEncoder;

  private final UserRepository userRepository;

  private final MarkerRepository markerRepository;

  private final LikedRepository likedRepository;

  public UserService(PasswordEncoder passwordEncoder, UserRepository userRepository, MarkerRepository markerRepository, LikedRepository likedRepository) {
    this.passwordEncoder = passwordEncoder;
    this.userRepository = userRepository;
    this.markerRepository = markerRepository;
    this.likedRepository = likedRepository;
  }


  //----------------------------Transactional method--------------------------------

  @Transactional
  private User save(User user) {
    return userRepository.save(user);
  }

  @Transactional
  public User join(String email, String password, String profImageName, String name) {
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

    User user;

    if (email.equals("icetime963@gmail.com"))
      user = new User(email,passwordEncoder.encode(password), profImageName, name,Role.ADMIN.name());
    else
      user = new User(email,passwordEncoder.encode(password), profImageName, name, Role.USER.name());
    return save(user);
  }


  @Transactional
  public User login(String email, String password) {
    checkArgument(isNotEmpty(password), "password must be provided.");
    checkArgument(isNotEmpty(email),"email must be provided.");

      User user = findByEmail(email).orElseThrow(() -> new NotFoundException("찾을 수 없습니다."));
    try {
      checkArgument(user.login(passwordEncoder, password),"비밀번호가 일치하지 않습니다.");
    } catch (Exception e) {
      throw new NotAcceptableException("Not Matched");
    }
      save(user);
      return user;
    }

    @Transactional
    @Modifying(clearAutomatically = true)
    public User changeName(Id<User,Long> id, String changeName) {
      User user = findById(id).orElseThrow(() -> new NotFoundException("찾을 수 없습니다."));
      try {
        checkArgument(findByName(changeName).isEmpty(),"This email already exist.");
        checkArgument(changeName.length() >= 2 && changeName.length() <= 10, "Invalid name length.");
      } catch (Exception e) {
        throw new NotAcceptableException("This email already exist.");
      }
      user.setUserName(changeName);
      save(user);
      return user;
    }

    @Transactional
    @Modifying(clearAutomatically = true)
    public User changePassword(Id<User,Long> id, String password, String changePassword) {
      User user = findById(id).orElseThrow(() -> new NotFoundException("찾을 수 없습니다."));
      try{
        checkArgument(user.isPasswordMatch(passwordEncoder, password),"not matched.");
      } catch (Exception e) {
        throw new UnauthorizedException("Not matched.");
      }
      try {
        checkArgument(PasswordValidation.isValidPassword(changePassword), "password validation failed.");
      } catch (Exception e) {
        throw new NotAcceptableException("Password validation failed.");
      }
      user.setPassword(passwordEncoder.encode(changePassword));
      save(user);
      return user;
    }

    @Transactional
    public void delete(Id<User,Long> id, String email, String password) {
      User user = findById(id).orElseThrow(() -> new NotFoundException("찾을 수 없습니다."));
      checkArgument(user.getEmail().equals(email), "이메일이 일치하지 않습니다.");
      checkArgument(user.isPasswordMatch(passwordEncoder, password), "비밀번호가 일치하지 않습니다.");
      userRepository.delete(user);
      cascadeLikes(id);
      cascadeMarkers(id);
    }

    // 이 메소드는 관리자 전용입니다.
    @Transactional
    @Modifying(clearAutomatically = true)
    public User changeRole(Id<User,Long> id, String userName, String changeRole) {
      checkArgument(findById(id).orElseThrow(() -> new NotFoundException("찾을 수 없습니다."))
              .getRole().equals(Role.ADMIN.name()),"관리자가 아닙니다.",HttpStatus.UNAUTHORIZED);

      User user = findByName(userName).orElseThrow(() -> new NotFoundException("찾을 수 없습니다."));

      Role role;

      if (changeRole.toUpperCase(Locale.ROOT).equals("GOODUSER")) {
        role = Role.GOODUSER;
      }
      else if (changeRole.toUpperCase(Locale.ROOT).equals("GREATUSER")) {
        role = Role.GREATUSER;
      }
      else if (changeRole.toUpperCase(Locale.ROOT).equals("USER")) {
        role = Role.USER;
      }
      else {
        throw new NotFoundException("찾을 수 없습니다.");
      }

      user.setRole(role.name());
      save(user);
      return user;
    }

    //-------------------------read only methods------------------------------------

    @Transactional(readOnly = true)
    public Optional<User> findById(Id<User, Long> userId) {
      checkArgument(userId != null, "markerId must be provided.",HttpStatus.BAD_REQUEST);

      return userRepository.findById(userId.value());
    }

    @Transactional(readOnly = true)
    public Optional<User> findByEmail(String email) {
      try {
        checkArgument(checkAddress(email), "Invalid email address: " + email, HttpStatus.BAD_REQUEST);
      } catch (Exception e) {
        throw new NotAcceptableException("email validation fail.");
      }
      checkArgument(email != null, "email must be provided.",HttpStatus.BAD_REQUEST);

      return Optional.ofNullable(userRepository.findByEmail(email));
    }

    @Transactional(readOnly = true)
    public Optional<User> findByName(String name) {
      checkArgument(name != null, "name must be provided.",HttpStatus.BAD_REQUEST);

      return Optional.ofNullable(userRepository.findByUserName(name));
    }

    private void cascadeMarkers(Id<User,Long> id) {
      List<Marker> markers = markerRepository.findAll();
      markers.forEach((element) -> {
        if (Objects.equals(element.getUserId(), id.value())) {
          markerRepository.delete(element);
        }
      });
    }

    private void cascadeLikes(Id<User,Long> id) {
      List<Liked> likeds = likedRepository.findAll();
      likeds.forEach((element) -> {
        if (Objects.equals(element.getUserId(),id.value()) || Objects.equals(element.getMfId(),id.value())) {
          likedRepository.delete(element);
        }
      });
    }
  }
