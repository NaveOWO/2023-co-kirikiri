package co.kirikiri.service;

import co.kirikiri.domain.ImageContentType;
import co.kirikiri.domain.member.EncryptedPassword;
import co.kirikiri.domain.member.Member;
import co.kirikiri.domain.member.MemberImage;
import co.kirikiri.domain.member.MemberProfile;
import co.kirikiri.domain.member.vo.Identifier;
import co.kirikiri.domain.member.vo.Nickname;
import co.kirikiri.exception.ConflictException;
import co.kirikiri.exception.NotFoundException;
import co.kirikiri.persistence.member.MemberRepository;
import co.kirikiri.service.dto.member.MemberInformationDto;
import co.kirikiri.service.dto.member.MemberInformationForPublicDto;
import co.kirikiri.service.dto.member.MemberJoinDto;
import co.kirikiri.service.dto.member.request.MemberJoinRequest;
import co.kirikiri.service.dto.member.response.MemberInformationForPublicResponse;
import co.kirikiri.service.dto.member.response.MemberInformationResponse;
import co.kirikiri.service.mapper.MemberMapper;
import java.net.URL;
import lombok.RequiredArgsConstructor;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class MemberService {

    private static final String DEFAULT_ORIGINAL_FILE_NAME_PROPERTY = "image.default.originalFileName";
    private static final String DEFAULT_SERVER_FILE_PATH_PROPERTY = "image.default.serverFilePath";
    private static final String DEFAULT_IMAGE_CONTENT_TYPE_PROPERTY = "image.default.imageContentType";
    private static final String DEFAULT_EXTENSION = "image.default.extension";

    private final MemberRepository memberRepository;
    private final Environment environment;
    private final NumberGenerator numberGenerator;
    private final FileService fileService;

    @Transactional
    public Long join(final MemberJoinRequest memberJoinRequest) {
        final MemberJoinDto memberJoinDto = MemberMapper.convertToMemberJoinDto(memberJoinRequest);
        checkIdentifierDuplicate(memberJoinDto.identifier());
        checkNicknameDuplicate(memberJoinDto.nickname());

        final EncryptedPassword encryptedPassword = new EncryptedPassword(memberJoinDto.password());
        final MemberProfile memberProfile = new MemberProfile(memberJoinDto.gender(),
                memberJoinDto.birthday(), memberJoinDto.phoneNumber());
        final Member member = new Member(memberJoinDto.identifier(), encryptedPassword,
                memberJoinDto.nickname(), findDefaultMemberImage(), memberProfile);
        return memberRepository.save(member).getId();
    }

    private void checkNicknameDuplicate(final Nickname nickname) {
        if (memberRepository.findByNickname(nickname).isPresent()) {
            throw new ConflictException("이미 존재하는 닉네임입니다.");
        }
    }

    private void checkIdentifierDuplicate(final Identifier identifier) {
        if (memberRepository.findByIdentifier(identifier).isPresent()) {
            throw new ConflictException("이미 존재하는 아이디입니다.");
        }
    }

    private MemberImage findDefaultMemberImage() {
        final String defaultOriginalFileName = environment.getProperty(DEFAULT_ORIGINAL_FILE_NAME_PROPERTY);
        final String defaultServerFilePath = environment.getProperty(DEFAULT_SERVER_FILE_PATH_PROPERTY);
        final String defaultImageContentType = environment.getProperty(DEFAULT_IMAGE_CONTENT_TYPE_PROPERTY);
        final String defaultExtension = environment.getProperty(DEFAULT_EXTENSION);
        final int randomImageNumber = numberGenerator.generate();
        return new MemberImage(defaultOriginalFileName + randomImageNumber,
                defaultServerFilePath + randomImageNumber + defaultExtension,
                ImageContentType.valueOf(defaultImageContentType));
    }

    public MemberInformationResponse findMemberInformation(final String identifier) {
        final Member memberWithInfo = findMemberInformationByIdentifier(identifier);
        final MemberInformationDto memberInformationDto = makeMemberInformationDto(memberWithInfo);
        return MemberMapper.convertToMemberInformationResponse(memberInformationDto);
    }

    public MemberInformationDto makeMemberInformationDto(final Member member) {
        final MemberImage memberImage = member.getImage();
        final MemberProfile memberProfile = member.getMemberProfile();
        final URL imageUrl = fileService.generateUrl(memberImage.getServerFilePath(), HttpMethod.GET);
        return new MemberInformationDto(member.getId(), member.getNickname().getValue(),
                imageUrl.toExternalForm(), memberProfile.getGender().name(), member.getIdentifier().getValue(),
                memberProfile.getPhoneNumber(), memberProfile.getBirthday());
    }

    private Member findMemberInformationByIdentifier(final String identifier) {
        return memberRepository.findWithMemberProfileAndImageByIdentifier(identifier)
                .orElseThrow(() -> new NotFoundException("존재하지 않는 회원입니다."));
    }

    public MemberInformationForPublicResponse findMemberInformationForPublic(final Long memberId) {
        final Member memberWithPublicInfo = findMemberInformationByMemberId(memberId);
        final URL memberimageURl = fileService.generateUrl(memberWithPublicInfo.getImage().getServerFilePath(),
                HttpMethod.GET);
        final MemberInformationForPublicDto memberInformationForPublicDto =
                new MemberInformationForPublicDto(memberWithPublicInfo.getNickname().getValue(),
                        memberimageURl.toExternalForm(),
                        memberWithPublicInfo.getMemberProfile().getGender().name());
        return MemberMapper.convertToMemberInformationForPublicResponse(memberInformationForPublicDto);
    }

    private Member findMemberInformationByMemberId(final Long memberId) {
        return memberRepository.findWithMemberProfileAndImageById(memberId)
                .orElseThrow(() -> new NotFoundException("존재하지 않는 회원입니다. memberId = " + memberId));
    }
}
