package reword;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import com.jcraft.jzlib.DeflaterOutputStream;

import org.apache.commons.codec.digest.DigestUtils;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
public class App {
    public static void main(String[] args) throws IOException
    {
        String pathToGitFolder = ".git";
        String inputCommit = "HEAD~1";
        
        String newCommitMessage = "New commit message";

        Repository repository = new FileRepositoryBuilder()
        .setGitDir(new File(pathToGitFolder))
        .build();
    
        String headString = "refs/heads/" + repository.getBranch();
        Ref head = repository.exactRef(headString);

        ObjectId toCommitId = head.getObjectId();
        String to = toCommitId.getName();

        ObjectId fromCommitId = repository.resolve(inputCommit);
        String from = fromCommitId.getName();

        RevWalk walk = new RevWalk(repository);

        RevCommit commit = walk.parseCommit(toCommitId);

        System.out.println("Start-Commit: " + commit + "\n");
        System.out.println("Walking all commits starting at " + to + " until we find " + from + "\n");

        walk.markStart(commit);

        ArrayList<RevCommit> commitsContent = new ArrayList<RevCommit>();
        int count = 0;

        for (RevCommit rev : walk) {
            System.out.println("Commit: " + rev);
            count++;
            commitsContent.add(rev);

            if (rev.getId().getName().equals(from)) {
                System.out.println("\nFound from, stopping walk. " + count);
                break;
            }
        }

        walk.dispose();
        walk.close();

        String header;
        RevCommit currentRevCommit;
        String currentCommitContent;
        String newCommitHash;

        String prevCommitHash = "";

        ByteArrayOutputStream baos;
        DeflaterOutputStream dos;

        FileOutputStream outputStream;
        File file;

        for (int i = commitsContent.size() - 1; i >= 0; i--) {
            currentRevCommit = commitsContent.get(i);
            currentCommitContent = new String(currentRevCommit.getRawBuffer());

            if (i == commitsContent.size() - 1) {
                currentCommitContent = currentCommitContent.replace(currentRevCommit.getFullMessage(), newCommitMessage);
            } else {
                currentCommitContent = currentCommitContent.replace("parent " + commitsContent.get(i + 1).getName(), "parent " + prevCommitHash);
            }

            header = "commit " + currentCommitContent.length() + "\0";

            currentCommitContent = header + currentCommitContent;

            newCommitHash = DigestUtils.sha1Hex(currentCommitContent);

            baos = new ByteArrayOutputStream();
            dos = new DeflaterOutputStream(baos);
            dos.write(currentCommitContent.getBytes());
            dos.flush();
            dos.close();
            file = new File(pathToGitFolder + "/objects/" + newCommitHash.substring(0, 2));
            file.mkdirs();
            outputStream = new FileOutputStream(pathToGitFolder + "/objects/" + newCommitHash.substring(0, 2) + "/" + newCommitHash.substring(2), false);
            baos.writeTo(outputStream);

            outputStream.close();
            baos.reset();

            prevCommitHash = newCommitHash;

            System.out.println(i);
        }

        outputStream = new FileOutputStream(pathToGitFolder + "/" + headString, false);
        outputStream.write((prevCommitHash + "\n").getBytes());
        outputStream.close();
    }
}